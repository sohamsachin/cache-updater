package com.example.cacheupdater.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "CACHE_VERSION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The active cache key used by the reading service
    @Column(name = "ACTIVE_CACHE_KEY")
    private String activeCacheKey;
}