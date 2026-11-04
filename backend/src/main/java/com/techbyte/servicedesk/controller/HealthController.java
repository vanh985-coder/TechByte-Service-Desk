package com.techbyte.servicedesk.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationAvailability availability;

    public HealthController(JdbcTemplate jdbcTemplate, ApplicationAvailability availability) {
        this.jdbcTemplate = jdbcTemplate;
        this.availability = availability;
    }

    @GetMapping({"/health", "/api/health"})
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping({"/ready", "/api/ready"})
    public ResponseEntity<Map<String, Object>> ready() {
        if (availability.getReadinessState() != ReadinessState.ACCEPTING_TRAFFIC) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "NOT_READY", "database", "UNKNOWN"
            ));
        }
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                return ResponseEntity.ok(Map.of(
                        "status", "READY",
                        "database", "UP"
                ));
            }
        } catch (Exception ignored) {
            // Do not expose DB connection credentials or stacktrace
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "NOT_READY",
                "database", "DOWN"
        ));
    }
}

