package com.microproject.linktoolkit.auth;

import com.microproject.linktoolkit.auth.dto.AuthResponse;
import com.microproject.linktoolkit.auth.dto.LoginRequest;
import com.microproject.linktoolkit.auth.dto.RegisterRequest;
import com.microproject.linktoolkit.exception.AliasAlreadyExistsException;
import com.microproject.linktoolkit.link.LinkRepository;
import com.microproject.linktoolkit.user.User;
import com.microproject.linktoolkit.user.UserRepository;
import com.microproject.linktoolkit.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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

        String jwtToken = jwtUtil.generateToken(savedUser);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        // This will automatically use our UserDetailsService and PasswordEncoder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // If the above line doesn't throw an exception, the user is authenticated
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication."));

        String jwtToken = jwtUtil.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}