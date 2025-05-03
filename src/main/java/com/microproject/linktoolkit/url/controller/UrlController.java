package com.microproject.linktoolkit.url.controller;

import com.microproject.linktoolkit.url.entity.Url;
import com.microproject.linktoolkit.url.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;

@CrossOrigin(origins = {"http://localhost:3000", "https://linktoolkit-ui.vercel.app/"})
@RestController
@RequestMapping("/")
public class UrlController {

    @Autowired
    private UrlService urlService;


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
     * @param longUrl The original long URL to be shortened (required).
     * @param life Optional ISO-8601 period (e.g. "P3D", "P1M", "P1Y2M"). Defaults to "P1M" if not provided.
     * @param expiresAt Optional ISO-8601 expiration timestamp (e.g. "2025-05-01T12:00:00Z").
     *                  If provided, it overrides the {@code life} parameter.
     * @param userId Optional user ID to associate the shortened URL with a specific user.
     * @return A ResponseEntity containing the shortened URL information if successful,
     *         or a 400 error for invalid inputs.
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
            @RequestParam String longUrl,
            @RequestParam(required = false, defaultValue = "P1M") String life,
            @RequestParam(required = false) String expiresAt,
            @RequestParam(required = false) Long userId
    ) {
        Instant now = Instant.now();
        Instant expiresAtInstant;

        if (expiresAt == null) {
            try {
                Period period = Period.parse(life);

                if (period.getYears() > 5) {
                    return ResponseEntity.badRequest().build();
                }

                // Add period to now (convert Instant to LocalDateTime temporarily)
                LocalDateTime localNow = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
                LocalDateTime futureLocal = localNow.plus(period);
                expiresAtInstant = futureLocal.toInstant(ZoneOffset.UTC);

            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            try {
                expiresAtInstant = Instant.parse(expiresAt);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Url shortened = urlService.shortenUrl(longUrl, now, expiresAtInstant,now, userId);
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
