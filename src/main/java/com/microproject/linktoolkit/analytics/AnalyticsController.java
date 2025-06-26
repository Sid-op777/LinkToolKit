package com.microproject.linktoolkit.analytics;

import com.microproject.linktoolkit.analytics.dto.LinkAnalyticsResponse;
import com.microproject.linktoolkit.exception.ResourceNotFoundException;
import com.microproject.linktoolkit.link.Link;
import com.microproject.linktoolkit.link.LinkRepository;
import com.microproject.linktoolkit.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/links") // Piggybacking on the /api/links route
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final LinkRepository linkRepository;

    @GetMapping("/{alias}/analytics")
    public ResponseEntity<LinkAnalyticsResponse> getLinkAnalytics(
            @PathVariable String alias,
            Authentication authentication
    ) {
        // Find the link by its alias
        Link link = linkRepository.findByShortAlias(alias)
                .orElseThrow(() -> new ResourceNotFoundException("Link with alias '" + alias + "' not found."));

        // === CRITICAL SECURITY CHECK ===
        // Ensure the authenticated user owns this link.
        User user = (User) authentication.getPrincipal(); // Get the authenticated User object
        if (link.getUser() == null || !link.getUser().getId().equals(user.getId())) {
            // If link has no owner or owner ID doesn't match, deny access.
            throw new AccessDeniedException("You do not have permission to view analytics for this link.");
        }

        // If the check passes, proceed to get the analytics data.
        LinkAnalyticsResponse analytics = analyticsService.getAnalyticsForLink(link.getId(), alias);

        return ResponseEntity.ok(analytics);
    }
}