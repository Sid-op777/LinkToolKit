package com.microproject.linktoolkit.auth.dto;

/**
 * A generic response for auth actions, containing the JWT.
 * @param token The generated JSON Web Token.
 */
public record AuthResponse(
        String token
) {}