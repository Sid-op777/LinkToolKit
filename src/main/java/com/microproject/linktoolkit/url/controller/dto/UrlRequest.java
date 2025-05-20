package com.microproject.linktoolkit.url.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UrlRequest {
    @NotBlank(message = "The long URL must not be empty.")
    private String longUrl;

    private String life;

    private String expiresAt;
}