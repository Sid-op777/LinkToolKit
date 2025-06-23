// src/main/java/com/microproject/linktoolkit/redirect/RedirectController.java

package com.microproject.linktoolkit.redirect;

import com.microproject.linktoolkit.analytics.AnalyticsService;
import com.microproject.linktoolkit.link.Link;
import com.microproject.linktoolkit.link.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final LinkRepository linkRepository;
    private final AnalyticsService analyticsService;

    @GetMapping("/{alias}")
    public void handleRedirect(
            @PathVariable String alias,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Optional<Link> linkOptional = linkRepository.findByShortAlias(alias);

        if (linkOptional.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Link link = linkOptional.get();

        // Check if the link is expired
        if (link.getExpiresAt().isBefore(Instant.now())) {
            // Treat expired links as not found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Trigger analytics logging asynchronously
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        analyticsService.logClick(link.getId(), ipAddress, userAgent, referer);

        // --- Set Cache-Control Headers to prevent caching ---
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Perform the redirect
        response.setStatus(HttpServletResponse.SC_FOUND); // 302 Found
        response.setHeader("Location", link.getLongUrl());
    }

    /**
     * Helper method to extract the real client IP address, accounting for proxies.
     * This logic now correctly lives in the web layer (the controller).
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // "X-Forwarded-For" can be a comma-separated list. The first one is the client.
        return ip.split(",")[0].trim();
    }
}