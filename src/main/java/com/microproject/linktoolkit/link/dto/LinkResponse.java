package com.microproject.linktoolkit.link.dto;

import java.time.Instant;

/**
 * DTO for representing a link in a list view (e.g., user dashboard).
 * @param shortUrl The full short URL.
 * @param longUrl The original long URL.
 * @param totalClicks The total number of clicks this link has received.
 * @param createdAt The timestamp when the link was created.
 * @param expiresAt The timestamp when the link will expire.
 */
public record LinkResponse(
        String shortUrl,
        String longUrl,
        long totalClicks,
        Instant createdAt,
        Instant expiresAt
) {
}