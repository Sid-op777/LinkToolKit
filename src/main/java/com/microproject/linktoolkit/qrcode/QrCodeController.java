package com.microproject.linktoolkit.qrcode;

import com.microproject.linktoolkit.link.dto.CreateLinkRequest;
import com.microproject.linktoolkit.qrcode.dto.QrCodeResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private static final String SESSION_COOKIE_NAME = "lnktk_session";

    @PostMapping
    public ResponseEntity<QrCodeResponse> createQrCode(
            @Valid @RequestBody CreateLinkRequest request,
            @CookieValue(name = SESSION_COOKIE_NAME, required = false) UUID anonymousSessionId,
            Authentication authentication,
            HttpServletResponse httpResponse) {

        Optional<String> userEmailOpt = Optional.empty();
        Optional<UUID> sessionIdOpt = Optional.ofNullable(anonymousSessionId);

        if (authentication != null && authentication.isAuthenticated()) {
            userEmailOpt = Optional.of(authentication.getName());
        } else if (sessionIdOpt.isEmpty()) {
            // First time anonymous user, generate and set a session cookie.
            UUID newSessionId = UUID.randomUUID();
            sessionIdOpt = Optional.of(newSessionId);
            setSessionCookie(httpResponse, newSessionId);
        }

        QrCodeResponse response = qrCodeService.generateAndUploadQrCode(request, userEmailOpt, sessionIdOpt);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private void setSessionCookie(HttpServletResponse response, UUID sessionId) {
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId.toString());
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(30));
        response.addCookie(sessionCookie);
    }
}