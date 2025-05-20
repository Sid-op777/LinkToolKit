package com.microproject.linktoolkit.auth.oauth;


import com.microproject.linktoolkit.auth.entity.User;
import com.microproject.linktoolkit.auth.repository.UserRepository;
import com.microproject.linktoolkit.auth.service.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2SuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Just a safety check (in case user wasn't saved earlier)
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.sendRedirect("http://localhost:3000/oauthFailure");
            return;
        }

        // Generate JWT and redirect
        String token = jwtUtil.generateToken(email);
        String redirectUrl = "http://localhost:3000/oauthSuccess?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
