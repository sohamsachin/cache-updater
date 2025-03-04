package com.example.cacheupdater.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse {
    private List<CacheRecordDto> data;
    private int totalPages;
}