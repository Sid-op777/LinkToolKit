package com.microproject.linktoolkit.analytics.dto;

import java.util.List;

/**
 * The main response DTO for the link analytics endpoint.
 * This structure is directly consumed by the frontend to build charts.
 */
public record LinkAnalyticsResponse(
        String id,
        String longUrl,
        String shortUrl,
        long totalClicks,
        List<TimeSeriesDataPoint> clicksOverTime,
        List<CategoryDataPoint> referrers,
        List<CategoryDataPoint> devices,
        List<CategoryDataPoint> locations
) {
}