package com.microproject.linktoolkit.analytics.dto;

/**
 * Represents a single data point for a categorical chart (e.g., a slice of a pie chart).
 * @param name The name of the category (e.g., "google.com", "Desktop", "USA").
 * @param count The value for this category.
 */
public record CategoryDataPoint(String name, Long count) {
}