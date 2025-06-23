package com.microproject.linktoolkit.link;

import com.microproject.linktoolkit.link.dto.CreateLinkRequest;
import com.microproject.linktoolkit.link.dto.CreateLinkResponse;
import com.microproject.linktoolkit.link.dto.LinkResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private static final String SESSION_COOKIE_NAME = "lnktk_session";

    @PostMapping
    public ResponseEntity<CreateLinkResponse> createShortLink(
            @Valid @RequestBody CreateLinkRequest request,
            @CookieValue(name = SESSION_COOKIE_NAME, required = false) UUID anonymousSessionId,
            Authentication authentication,
            HttpServletResponse httpResponse) {

        CreateLinkResponse response;
        Optional<String> userEmailOpt = Optional.empty();
        Optional<UUID> sessionIdOpt = Optional.ofNullable(anonymousSessionId);

        if (authentication != null && authentication.isAuthenticated()) {
            // User is authenticated
            userEmailOpt = Optional.of(authentication.getName());
            response = linkService.createLink(request, userEmailOpt, Optional.empty());
        } else {
            // User is anonymous
            if (sessionIdOpt.isEmpty()) {
                // First time anonymous user, generate a session ID
                UUID newSessionId = UUID.randomUUID();
                sessionIdOpt = Optional.of(newSessionId);
                setSessionCookie(httpResponse, newSessionId);
            }
            response = linkService.createLink(request, userEmailOpt, sessionIdOpt);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getUserLinks(Authentication authentication) {
        // This endpoint requires authentication, which will be enforced by Spring Security.
        // If the code reaches here, authentication is valid.
        String userEmail = authentication.getName();
        List<LinkResponse> links = linkService.getLinksForUser(userEmail);
        return ResponseEntity.ok(links);
    }

    private void setSessionCookie(HttpServletResponse response, UUID sessionId) {
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId.toString());
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true); // Should be true in production
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(30)); // Cookie lasts for 30 days
        response.addCookie(sessionCookie);
    }
}