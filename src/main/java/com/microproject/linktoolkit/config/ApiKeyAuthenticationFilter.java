package com.microproject.linktoolkit.config;

import com.microproject.linktoolkit.user.User;
import com.microproject.linktoolkit.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String apiKeyHeader = request.getHeader("X-API-Key");

        if (apiKeyHeader == null || apiKeyHeader.isBlank() || !apiKeyHeader.contains(".")) {
            filterChain.doFilter(request, response); // No API key, pass to the next filter (JWT)
            return;
        }

        // --- API Key Found, Process It ---
        String[] keyParts = apiKeyHeader.split("\\.", 2);
        if (keyParts.length != 2) {
            filterChain.doFilter(request, response);
            return;
        }

        String publicId = keyParts[0];
        String secretKey = keyParts[1];

        // Find user by the public part of the key
        userRepository.findByApiKeyPublicId(publicId).ifPresent(user -> {
            // If user is found, securely compare the secret part with the stored hash
            if (passwordEncoder.matches(secretKey, user.getApiKeyHash())) {
                // Key is valid, create authentication token and set it in the context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        });

        filterChain.doFilter(request, response);
    }
}