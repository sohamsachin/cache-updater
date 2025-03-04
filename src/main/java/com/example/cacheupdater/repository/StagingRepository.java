package com.example.cacheupdater.repository;

import com.example.cacheupdater.entity.CacheRecordStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagingRepository extends JpaRepository<CacheRecordStaging, Long> {
}