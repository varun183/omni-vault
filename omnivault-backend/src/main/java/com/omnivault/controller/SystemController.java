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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
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
}