package com.example.cacheupdater.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "CACHE_RECORD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FIELD1")
    private String field1;

    @Column(name = "FIELD2")
    private String field2;

    // Field to hold the cache version key (timestamp or version string)
    @Column(name = "CACHE_KEY")
    private String cacheKey;
}