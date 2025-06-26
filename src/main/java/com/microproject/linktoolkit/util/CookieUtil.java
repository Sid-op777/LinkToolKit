package com.microproject.linktoolkit.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "rt";

    public void createRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);
        refreshTokenCookie.setHttpOnly(true);   // Prevents access from JavaScript
        refreshTokenCookie.setSecure(true);     // Only send over HTTPS
        refreshTokenCookie.setPath("/");        // Available for all paths
        refreshTokenCookie.setMaxAge((int) maxAgeSeconds);
        response.addCookie(refreshTokenCookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Expire the cookie immediately
        response.addCookie(refreshTokenCookie);
    }
}