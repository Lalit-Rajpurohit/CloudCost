package com.cloudmonitor.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check controller.
 *
 * Provides a basic health endpoint for monitoring and load balancers.
 * For more comprehensive health checks, use Spring Actuator endpoints.
 */
@RestController
public class HealthController {

    @Value("${spring.application.name:cloud-cost-monitor}")
    private String applicationName;

    /**
     * Basic health check endpoint.
     *
     * Example: GET /health
     *
     * @return Health status with timestamp
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(health);
    }

    /**
     * Application info endpoint.
     *
     * Example: GET /info
     *
     * @return Application information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("description", "Cloud Cost Monitoring Tool for AWS");
        info.put("version", "1.0.0");

        return ResponseEntity.ok(info);
    }
}
