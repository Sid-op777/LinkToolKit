package com.microproject.linktoolkit.url.service;

import com.microproject.linktoolkit.auth.repository.UserRepository;
import com.microproject.linktoolkit.url.entity.Url;
import com.microproject.linktoolkit.url.repository.UrlRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UrlService {
    //note
    //Field Injection: Bad – It's harder to test, not as clear about dependencies, and can lead to issues with immutability.
    //
    //Constructor Injection: Good – It's more testable, encourages immutability, and makes dependencies explicit.
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    @Autowired
    public UrlService(UrlRepository urlRepository, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
    }

    public Url shortenUrl(String longUrl, Instant now, Instant expiresAt, Instant lastVisitedAt, Long userId) {
        String shortUrl = generateShortCode(longUrl);

        Url url = new Url();
        url.setId(shortUrl);
        url.setLongUrl(longUrl);
        url.setCreatedAt(now);
        url.setExpiresAt(expiresAt);
        url.setUserId(userId);
        url.setLastVisited(lastVisitedAt);

        return urlRepository.save(url);
    }

    public Optional<Url> getOriginalUrl(String shortCode) {
        return urlRepository.findById(shortCode);
    }

    private String generateShortCode(String input) {
        return DigestUtils.sha256Hex(UUID.randomUUID().toString()).substring(0, 6);
    }

    public Long resolveUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null); // Return null for anonymous users
    }
}