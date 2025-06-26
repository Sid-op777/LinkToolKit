package com.microproject.linktoolkit.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller that exposes system health check endpoints.
 * <p>
 * This controller provides a simple health check at <code>/api/health</code>
 * which can be used by load balancers, uptime monitors, or other external systems
 * to verify that the application is running.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Health check endpoint.
     * <p>
     * Responds with HTTP 200 OK and a simple JSON payload indicating system status.
     * </p>
     *
     * @return a {@link ResponseEntity} containing a JSON map with a <code>"status"</code> key set to <code>"OK"</code>
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity
                .ok(Map.of("status", "OK"));
    }
}
