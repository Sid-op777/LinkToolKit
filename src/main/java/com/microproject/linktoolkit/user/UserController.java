package com.microproject.linktoolkit.user;

import com.microproject.linktoolkit.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        // The user's email is the 'name' in the Authentication object
        String email = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/api-key")
    public ResponseEntity<UserProfileResponse> generateNewApiKey(Authentication authentication) {
        String email = authentication.getName();
        // The service returns the response containing the raw, one-time key
        UserProfileResponse profileWithNewKey = userService.generateApiKey(email);
        return ResponseEntity.ok(profileWithNewKey);
    }
}