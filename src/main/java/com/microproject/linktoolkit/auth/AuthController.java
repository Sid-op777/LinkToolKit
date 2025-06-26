package com.microproject.linktoolkit.auth;

import com.microproject.linktoolkit.auth.dto.AuthResponse;
import com.microproject.linktoolkit.auth.dto.LoginRequest;
import com.microproject.linktoolkit.auth.dto.RegisterRequest;
import com.microproject.linktoolkit.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint for user registration.
     * @param request The registration request containing email, password, and optional session ID.
     * @return A response entity containing the JWT for the newly registered user.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Endpoint for user login.
     * @param request The login request containing email and password.
     * @return A response entity containing the JWT for the authenticated user.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            // Read the token from the HttpOnly cookie
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME) String refreshToken
    ) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
}