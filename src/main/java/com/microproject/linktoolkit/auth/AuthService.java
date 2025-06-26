package com.microproject.linktoolkit.auth;

import com.microproject.linktoolkit.auth.dto.AuthResponse;
import com.microproject.linktoolkit.auth.dto.LoginRequest;
import com.microproject.linktoolkit.auth.dto.RegisterRequest;
import com.microproject.linktoolkit.exception.AliasAlreadyExistsException;
import com.microproject.linktoolkit.exception.ResourceNotFoundException;
import com.microproject.linktoolkit.link.LinkRepository;
import com.microproject.linktoolkit.user.User;
import com.microproject.linktoolkit.user.UserRepository;
import com.microproject.linktoolkit.util.CookieUtil;
import com.microproject.linktoolkit.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            // Using a familiar exception type, but the message is key.
            throw new AliasAlreadyExistsException("User with email " + request.email() + " already exists.");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        User savedUser = userRepository.save(user);

        // Claim anonymous links if a session ID is provided
        if (request.anonymousSessionId() != null) {
            linkRepository.claimAnonymousLinks(savedUser.getId(), request.anonymousSessionId());
        }

        String accessToken = jwtUtil.generateToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        // Store the hash of the refresh token
        user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        // Set the refresh token in a secure HttpOnly cookie
        CookieUtil cookieUtil = new CookieUtil();
        cookieUtil.createRefreshTokenCookie(response, refreshToken, refreshExpirationMs / 1000);

        return new AuthResponse(accessToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // This will automatically use our UserDetailsService and PasswordEncoder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // If the above line doesn't throw an exception, the user is authenticated
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication."));

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Store the hash of the refresh token
        user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        // Set the refresh token in a secure HttpOnly cookie
        CookieUtil cookieUtil = new CookieUtil();
        cookieUtil.createRefreshTokenCookie(response, refreshToken, refreshExpirationMs / 1000);

        return new AuthResponse(accessToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        String userEmail = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found from refresh token"));

        User user = (User) userDetails;

        // Check if the provided token matches the hashed one in the DB
        if (!passwordEncoder.matches(refreshToken, user.getRefreshTokenHash()) || !jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // If valid, issue a new access token
        String newAccessToken = jwtUtil.generateToken(userDetails);
        return new AuthResponse(newAccessToken);
    }
}