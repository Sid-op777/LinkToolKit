package com.microproject.linktoolkit.user.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for representing a user's profile information.
 * @param id The user's unique ID.
 * @param email The user's email address.
 * @param apiKey The user's API key. This will be null if not yet generated.
 * @param createdAt The timestamp when the user account was created.
 */
public record UserProfileResponse(
        UUID id,
        String email,
        String apiKey, // NOTE: This is the raw key, only shown once upon generation.
        Instant createdAt
) {
}