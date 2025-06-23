package com.microproject.linktoolkit.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UrlAnalyticsResponse {
    private long totalClicks;
    private String topReferrer;
    private String topDevice;
    private String topCountry;
    private Map<String, Long> clicksByDay;       // e.g., "2025-06-01" -> 50
    private Map<String, Long> referrerStats;     // e.g., "google.com" -> 100
    private Map<String, Long> deviceStats;       // e.g., "Mobile" -> 120
    private Map<String, Long> countryStats;      // e.g., "IN" -> 80
}