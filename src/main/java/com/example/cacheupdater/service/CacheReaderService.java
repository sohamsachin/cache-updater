package com.example.cacheupdater.service;

import com.example.cacheupdater.entity.CacheRecord;
import com.example.cacheupdater.entity.CacheVersion;
import com.example.cacheupdater.repository.CacheRepository;
import com.example.cacheupdater.repository.CacheVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheReaderService {

    private final CacheRepository cacheRepository;
    private final CacheVersionRepository cacheVersionRepository;

    public CacheReaderService(CacheRepository cacheRepository, CacheVersionRepository cacheVersionRepository) {
        this.cacheRepository = cacheRepository;
        this.cacheVersionRepository = cacheVersionRepository;
    }

    public List<CacheRecord> getActiveCacheRecords() {
        String activeCacheKey = cacheVersionRepository.findActiveVersion()
                                    .map(CacheVersion::getActiveCacheKey)
                                    .orElseThrow(() -> new IllegalStateException("Active cache key not set"));
        return cacheRepository.findByCacheKey(activeCacheKey);
    }
}