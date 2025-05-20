package com.microproject.linktoolkit.url.controller;

import com.microproject.linktoolkit.url.controller.dto.UrlRequest;
import com.microproject.linktoolkit.url.entity.Url;
import com.microproject.linktoolkit.url.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "https://linktoolkit-ui.vercel.app/"})
@RestController
@RequestMapping("/")
public class UrlController {

    @Autowired
    private UrlService urlService;

    //TODO:fix this javadoc
    //TODO:update swagger
    //Todo: whitelabel, no fallback
    /**
     * Creates a shortened URL with an optional expiration timestamp or duration.
     *
     * <p>This endpoint allows clients to shorten a URL. Expiration can be specified in two ways:
     * <ul>
     *     <li>By providing a direct {@code expiresAt} timestamp in ISO-8601 format.</li>
     *     <li>By using an ISO-8601 period string via the {@code life} parameter (e.g. "P10D", "P1M", "P1Y2M").</li>
     * </ul>
     * If both {@code expiresAt} and {@code life} are provided, the {@code expiresAt} timestamp will take precedence.
     * If neither is provided, the default expiration is 1 month from the current time.
     * Period values exceeding 5 years will be rejected.
     *
     *
     */
    @Operation(
            summary = "Shorten a URL",
            description = "Creates a shortened URL with an optional expiration time or duration. "
                    + "Expiration can be defined using either a timestamp (expiresAt) or a duration (life). "
                    + "If both are provided, the timestamp takes precedence."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully shortened URL"),
            @ApiResponse(responseCode = "400", description = "Invalid expiration format or value")
    })
    @Parameters({
            @Parameter(name = "longUrl", description = "The original long URL to shorten", required = true),
            @Parameter(name = "life", description = "ISO-8601 period (e.g. P10D, P1M, P1Y2M). Max allowed: 5 years.", required = false),
            @Parameter(name = "expiresAt", description = "ISO-8601 expiration timestamp (e.g. 2025-05-01T12:00:00Z). Overrides 'life' if provided.", required = false),
            @Parameter(name = "userId", description = "Optional user ID for associating the shortened URL", required = false)
    })
    @PostMapping("/shorten")
    public ResponseEntity<Url> createShortUrl(
            @Valid @RequestBody UrlRequest request,
            @AuthenticationPrincipal String userEmail
    ) {
        Instant now = Instant.now();
        Instant expiresAtInstant;

        try{
            if(request.getExpiresAt() != null) {
                expiresAtInstant = Instant.parse(request.getExpiresAt());
            }
            else {
                Period period = Period.parse(request.getLife());

                if (period.getYears() > 5) {
                    return ResponseEntity.badRequest().build();
                }

                LocalDateTime future = LocalDateTime.ofInstant(now, ZoneOffset.UTC).plus(period);
                expiresAtInstant = future.toInstant(ZoneOffset.UTC);
            }

        }
        catch (DateTimeParseException e){
            return ResponseEntity.badRequest().build();
        }

        Long userId = null;
        if (userEmail != null) {
            userId = urlService.resolveUserIdByEmail(userEmail);
        }

        Url shortened = urlService.shortenUrl(request.getLongUrl(), now, expiresAtInstant,now, userId);
        return ResponseEntity.ok(shortened);
    }

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        Optional<Url> optionalUrl = urlService.getOriginalUrl(shortCode);
        if (optionalUrl.isPresent()) {
            Url url = optionalUrl.get();
            url.setLastVisited(Instant.now());

            response.setStatus(HttpServletResponse.SC_FOUND); // 302
            response.setHeader("Location", url.getLongUrl());
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body("Server is online");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server is offline");
        }
    }
}
