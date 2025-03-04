package com.example.cacheupdater.service;

import com.example.cacheupdater.dto.CacheRecordDto;
import com.example.cacheupdater.entity.CacheRecord;
import com.example.cacheupdater.entity.CacheRecordStaging;
import com.example.cacheupdater.entity.CacheVersion;
import com.example.cacheupdater.repository.CacheRepository;
import com.example.cacheupdater.repository.CacheVersionRepository;
import com.example.cacheupdater.repository.StagingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CacheService {

    private final StagingRepository stagingRepository;
    private final CacheRepository cacheRepository;
    private final CacheVersionRepository cacheVersionRepository;

    public CacheService(StagingRepository stagingRepository, 
                        CacheRepository cacheRepository, 
                        CacheVersionRepository cacheVersionRepository) {
        this.stagingRepository = stagingRepository;
        this.cacheRepository = cacheRepository;
        this.cacheVersionRepository = cacheVersionRepository;
    }

    @Transactional
    public void loadIntoStaging(List<CacheRecordDto> dataDtos, String newCacheKey) {
        log.info("Loading {} records into staging table with cacheKey: {}", dataDtos.size(), newCacheKey);
        List<CacheRecordStaging> stagingRecords = dataDtos.stream()
            .map(dto -> new CacheRecordStaging(null, dto.getField1(), dto.getField2(), newCacheKey))
            .collect(Collectors.toList());
        stagingRepository.deleteAllInBatch();
        stagingRepository.saveAll(stagingRecords);
        log.info("Staging table loaded successfully.");
    }

    @Transactional
    public void promoteStagingToLive(String newCacheKey) {
        log.info("Promoting staging data with cacheKey {} to live cache.", newCacheKey);
        List<CacheRecordStaging> stagingData = stagingRepository.findAll();
        List<CacheRecord> liveRecords = stagingData.stream()
            .map(staging -> new CacheRecord(null, staging.getField1(), staging.getField2(), newCacheKey))
            .collect(Collectors.toList());
        cacheRepository.saveAll(liveRecords);
        CacheVersion version = cacheVersionRepository.findActiveVersion()
            .orElse(new CacheVersion(1L, null));
        version.setActiveCacheKey(newCacheKey);
        cacheVersionRepository.save(version);
        log.info("Active cache key updated to {}.", newCacheKey);
    }
}