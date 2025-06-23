package com.microproject.linktoolkit.analytics.dto;

/**
 * Represents a single data point on a time-series chart.
 * @param date The date for the data point (e.g., "2023-10-27").
 * @param clicks The number of clicks on that date.
 */
public record TimeSeriesDataPoint(String date, Long clicks) {
}