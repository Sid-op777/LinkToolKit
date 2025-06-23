package com.microproject.linktoolkit.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for creating a new short link.
 * @param longUrl The original URL to be shortened. Must be a valid URL.
 * @param alias An optional custom alias for the short link.
 * @param expiry An optional ISO-8601 period string (e.g., "P1M" for 1 month).
 */
public record CreateLinkRequest(
        @NotBlank(message = "URL cannot be blank.")
        @URL(message = "A valid URL format is required.")
        String longUrl,

        @Size(min = 3, max = 50, message = "Alias must be between 3 and 50 characters.")
        @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Alias can only contain alphanumeric characters, underscores, and hyphens.")
        String alias,

        String expiry
) {
}