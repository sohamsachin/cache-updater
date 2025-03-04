package com.example.cacheupdater.scheduler;

import com.example.cacheupdater.entity.JobLog;
import com.example.cacheupdater.repository.JobLogRepository;
import com.example.cacheupdater.service.CacheService;
import com.example.cacheupdater.service.DataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class CacheUpdateScheduler {

    private final DataFetchService dataFetchService;
    private final CacheService cacheService;
    private final JobLogRepository jobLogRepository;

    public CacheUpdateScheduler(DataFetchService dataFetchService,
                                CacheService cacheService,
                                JobLogRepository jobLogRepository) {
        this.dataFetchService = dataFetchService;
        this.cacheService = cacheService;
        this.jobLogRepository = jobLogRepository;
    }

    // Scheduled to run at startup and every 6 hours.
    @Scheduled(cron = "0 0 0/6 * * *")
    public void updateCacheJob() {
        JobLog jobLog = new JobLog();
        jobLog.setJobName("CacheUpdateJob");
        jobLog.setStartTime(LocalDateTime.now());
        jobLog.setStatus("IN_PROGRESS");
        jobLog = jobLogRepository.save(jobLog);

        try {
            log.info("Cache update job started at {}", jobLog.getStartTime());
            String newCacheKey = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());

            // Directly fetch and persist all data with the new cache key.
            dataFetchService.fetchAllData(newCacheKey);

            // Promote the staging data to the live cache.
            cacheService.promoteStagingToLive(newCacheKey);

            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setDuration(Duration.between(jobLog.getStartTime(), jobLog.getEndTime()).toMillis());
            jobLog.setStatus("SUCCESS");
            jobLogRepository.save(jobLog);
            log.info("Cache update job completed successfully at {}", jobLog.getEndTime());
        } catch (InterruptedException | ExecutionException e) {
            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setDuration(Duration.between(jobLog.getStartTime(), jobLog.getEndTime()).toMillis());
            jobLog.setStatus("FAILED");
            jobLog.setErrorMessage(e.getMessage());
            jobLogRepository.save(jobLog);
            log.error("Cache update job failed: ", e);
        }
    }
}