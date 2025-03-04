package com.example.cacheupdater.repository;

import com.example.cacheupdater.entity.CacheVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CacheVersionRepository extends JpaRepository<CacheVersion, Long> {
    @Query("SELECT cv FROM CacheVersion cv WHERE cv.id = 1")
    Optional<CacheVersion> findActiveVersion();
}