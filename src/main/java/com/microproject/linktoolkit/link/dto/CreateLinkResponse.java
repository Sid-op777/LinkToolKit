package com.microproject.linktoolkit.link.dto;

import java.time.Instant;

/**
 * DTO for the response after successfully creating a short link.
 * @param shortUrl The full, clickable short URL.
 * @param longUrl The original long URL.
 * @param expiresAt The exact timestamp when the link will expire.
 */
public record CreateLinkResponse(
        String shortUrl,
        String longUrl,
        Instant expiresAt
) {
}