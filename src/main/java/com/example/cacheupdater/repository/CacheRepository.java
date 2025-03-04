package com.example.cacheupdater.repository;

import com.example.cacheupdater.entity.CacheRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheRepository extends JpaRepository<CacheRecord, Long> {
    List<CacheRecord> findByCacheKey(String cacheKey);
}