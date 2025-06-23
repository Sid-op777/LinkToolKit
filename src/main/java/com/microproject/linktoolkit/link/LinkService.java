package com.microproject.linktoolkit.link;

import com.microproject.linktoolkit.analytics.ClickRepository;
import com.microproject.linktoolkit.exception.AliasAlreadyExistsException;
import com.microproject.linktoolkit.exception.ReservedAliasException;
import com.microproject.linktoolkit.link.dto.CreateLinkRequest;
import com.microproject.linktoolkit.link.dto.CreateLinkResponse;
import com.microproject.linktoolkit.link.dto.LinkResponse;
import com.microproject.linktoolkit.user.User;
import com.microproject.linktoolkit.user.UserRepository;
import com.microproject.linktoolkit.util.AliasGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final AliasGenerator aliasGenerator;
    private final ClickRepository clickRepository;

    // A simple set of reserved words to prevent route conflicts
    private static final Set<String> RESERVED_ALIASES = Set.of(
            "api", "auth", "user", "links", "analytics", "qrcode", "bulk", "login", "register", "logout", "admin"
    );

    // Injecting the base URL from application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional // Ensures the entire method runs in a single database transaction
    public CreateLinkResponse createLink(CreateLinkRequest request, Optional<String> userEmailOpt, Optional<UUID> anonymousSessionIdOpt) {
        String alias = determineAlias(request.alias());
        Instant expiresAt = determineExpiry(request.expiry());

        Link newLink = Link.builder()
                .longUrl(request.longUrl())
                .shortAlias(alias)
                .expiresAt(expiresAt)
                .build();

        // Associate with user or session
        if (userEmailOpt.isPresent()) {
            User user = userRepository.findByEmail(userEmailOpt.get())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
            newLink.setUser(user);
        } else if (anonymousSessionIdOpt.isPresent()) {
            newLink.setAnonymousSessionId(anonymousSessionIdOpt.get());
        }

        Link savedLink = linkRepository.save(newLink);

        return new CreateLinkResponse(
                baseUrl + "/" + savedLink.getShortAlias(),
                savedLink.getLongUrl(),
                savedLink.getExpiresAt()
        );
    }

    private String determineAlias(String customAlias) {
        if (customAlias != null && !customAlias.isBlank()) {
            // User provided a custom alias
            if (RESERVED_ALIASES.contains(customAlias.toLowerCase())) {
                throw new ReservedAliasException("Alias '" + customAlias + "' is a reserved word.");
            }
            if (linkRepository.existsByShortAlias(customAlias)) {
                throw new AliasAlreadyExistsException("Alias '" + customAlias + "' is already taken.");
            }
            return customAlias;
        } else {
            // Generate a unique random alias
            String generatedAlias;
            do {
                generatedAlias = aliasGenerator.generate();
            } while (linkRepository.existsByShortAlias(generatedAlias)); // Ensure uniqueness
            return generatedAlias;
        }
    }

    private Instant determineExpiry(String expiryString) {
        final Period defaultExpiry = Period.ofMonths(1);
        final Period maxExpiry = Period.ofYears(5);

        if (expiryString == null || expiryString.isBlank()) {
            return Instant.now().plus(defaultExpiry.getDays() + (long) defaultExpiry.getMonths() * 30, ChronoUnit.DAYS);
        }

        try {
            Period requestedPeriod = Period.parse(expiryString);
            long requestedDays = requestedPeriod.toTotalMonths() * 30 + requestedPeriod.getDays();
            long maxDays = maxExpiry.toTotalMonths() * 30 + maxExpiry.getDays();

            if (requestedDays > maxDays) {
                throw new IllegalArgumentException("Expiry period cannot exceed 5 years.");
            }
            return Instant.now().plus(requestedDays, ChronoUnit.DAYS);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiry period format. Please use ISO-8601 period format (e.g., P1M, P10D).");
        }
    }

    // Add this method inside the LinkService class

    public List<LinkResponse> getLinksForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + userEmail));

        List<Link> links = linkRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // Map the Link entities to our LinkResponse DTO
        return links.stream()
                .map(link -> {
                    // For each link, fetch its total click count.
                    // Note: This is a simple implementation. For very high performance, this could
                    // be optimized into a single query to avoid the N+1 problem.
                    long totalClicks = clickRepository.countByLinkId(link.getId());

                    return new LinkResponse(
                            baseUrl + "/" + link.getShortAlias(),
                            link.getLongUrl(),
                            totalClicks,
                            link.getCreatedAt(),
                            link.getExpiresAt()
                    );
                })
                .collect(Collectors.toList());
    }
}