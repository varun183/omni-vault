package com.omnivault.controller;

import com.omnivault.config.AwsS3Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8080"})
@Tag(name = "System", description = "Operation for checking service status")
public class SystemController {

    private final AwsS3Config s3Config;

    @Operation(
            summary = "Check cloud storage status",
            description = "Provides information about the current cloud storage configuration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cloud storage status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/cloud-status")
    public ResponseEntity<Map<String, Boolean>> getCloudStorageStatus() {
        return ResponseEntity.ok(Map.of(
                "cloudStorageEnabled", s3Config.isEnabled()
        ));
    }

    @Operation(
            summary = "Check system health",
            description = "Simple endpoint to verify the system is up and running"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System is healthy",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // Add explicit OPTIONS method handling
    @RequestMapping(value = "/health", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleHealthOptions() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/cloud-status", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleCloudStatusOptions() {
        return ResponseEntity.ok().build();
    }

    // Add a system info endpoint
    @Operation(
            summary = "Get system information",
            description = "Provides detailed information about the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System information retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        // Get JVM uptime in milliseconds
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();

        // Convert to a more readable format
        long uptimeSeconds = uptimeMillis / 1000;
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;
        long uptimeDays = uptimeHours / 24;

        // Format uptime string
        String uptime = String.format("%d days, %d hours, %d minutes",
                uptimeDays, uptimeHours % 24, uptimeMinutes % 60);

        return ResponseEntity.ok(Map.of(
                "version", "1.0.0", // Replace with actual version from application properties
                "startTime", Instant.now().toString(),
                "uptime", uptime,
                "serverTime", Instant.now().toString()
        ));
    }
}