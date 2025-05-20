package com.microproject.linktoolkit.auth.oauth.service;

import com.microproject.linktoolkit.auth.entity.User;
import com.microproject.linktoolkit.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // e.g. "google"
        String providerId = user.getAttribute("sub");
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");

        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser == null) {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .createdAt(Instant.now())
                    .build();
            userRepository.save(newUser);
        }

        return user;
    }
}