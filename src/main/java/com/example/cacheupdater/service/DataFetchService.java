package com.example.cacheupdater.service;

import com.example.cacheupdater.dto.CacheRecordDto;
import com.example.cacheupdater.dto.PaginatedResponse;
import com.example.cacheupdater.entity.CacheRecordStaging;
import com.example.cacheupdater.repository.StagingRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataFetchService {

    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    private final RateLimiter rateLimiter;
    private final StagingRepository stagingRepository;

    @Value("${provider.api.baseUrl}")
    private String providerApiBaseUrl;

    public DataFetchService(StagingRepository stagingRepository) {
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newFixedThreadPool(10);
        this.rateLimiter = RateLimiter.of("providerApi", RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(500))
                .build());
        this.stagingRepository = stagingRepository;
    }

    /**
     * Fetches all data from the external API and directly persists each page into the staging table
     * using the provided cacheKey.
     *
     * @param cacheKey the cache key (version) to assign to each record.
     */
    public void fetchAllData(String cacheKey) throws InterruptedException, ExecutionException {
        int page = 0;
        int pageSize = 1000; // Adjust chunk size as needed

        // Fetch first page to determine total pages.
        PaginatedResponse initialResponse = fetchPage(page, pageSize);
        int totalPages = initialResponse.getTotalPages();
        log.info("Total pages available: {}", totalPages);

        // Persist the initial page directly.
        persistData(initialResponse.getData(), cacheKey);

        // For each subsequent page, fetch asynchronously and persist.
        List<CompletableFuture<Void>> futures =
                java.util.stream.IntStream.range(1, totalPages)
                        .mapToObj(currentPage -> CompletableFuture.runAsync(() -> {
                            try {
                                PaginatedResponse response = fetchPage(currentPage, pageSize);
                                persistData(response.getData(), cacheKey);
                            } catch (Exception e) {
                                log.error("Error fetching page {}: {}", currentPage, e.getMessage());
                                throw new RuntimeException("Error fetching page " + currentPage, e);
                            }
                        }, executorService))
                        .collect(Collectors.toList());

        // Wait for all asynchronous tasks to complete.
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Persists a list of CacheRecordDto records into the staging table with the given cacheKey.
     *
     * @param dtos     the list of data transfer objects fetched from the API.
     * @param cacheKey the cache key to be stored with each record.
     */
    private void persistData(List<CacheRecordDto> dtos, String cacheKey) {
        List<CacheRecordStaging> stagingRecords = dtos.stream()
                .map(dto -> new CacheRecordStaging(null, dto.getField1(), dto.getField2(), cacheKey))
                .collect(Collectors.toList());
        stagingRepository.saveAll(stagingRecords);
        log.info("Persisted {} records with cacheKey {}", stagingRecords.size(), cacheKey);
    }

    @Retryable(
            value = { HttpServerErrorException.class, ResourceAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public PaginatedResponse fetchPage(int page, int pageSize) {
        // Wait for permission from the rate limiter.
        RateLimiter.waitForPermission(rateLimiter);
        String url = providerApiBaseUrl + "/data?page=" + page + "&size=" + pageSize;
        log.info("Fetching page {} from URL: {}", page, url);
        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(url, PaginatedResponse.class);
        return response.getBody();
    }

    @Recover
    public PaginatedResponse recoverFetchPage(Exception e, int page, int pageSize) {
        log.error("Failed to fetch page {} after retries. Exception: {}", page, e.getMessage());
        throw new RuntimeException("Unable to fetch page " + page + " after multiple attempts", e);
    }
}