package com.microproject.linktoolkit.user;

import com.microproject.linktoolkit.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // A prefix for our API keys to make them identifiable
    private static final String API_KEY_PREFIX = "lk_";

    public UserProfileResponse getUserProfile(String email) {
        User user = findUserByEmail(email);
        // We do not return the API key here for security reasons,
        // only upon generation. We can return the prefix or a masked version if needed.
        String displayApiKey = user.getApiKeyHash() != null ? API_KEY_PREFIX + "..." : null;

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                displayApiKey, // Return a masked key
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileResponse generateApiKey(String email) {
        User user = findUserByEmail(email);

        // Define prefixes for clarity
        final String publicKeyPrefix = "lkp_";
        final String secretKeyPrefix = "lks_";

        // 1. Generate two random parts
        String publicId = publicKeyPrefix + UUID.randomUUID().toString().replace("-", "");
        String secretKey = secretKeyPrefix + UUID.randomUUID().toString().replace("-", "");

        // 2. Store the public part in plaintext and the hash of the secret part
        user.setApiKeyPublicId(publicId);
        user.setApiKeyHash(passwordEncoder.encode(secretKey));
        userRepository.save(user);

        // 3. Combine them for the user to see ONCE.
        String fullApiKey = publicId + "." + secretKey;

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                fullApiKey, // Return the full key this one time
                user.getCreatedAt()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }
}