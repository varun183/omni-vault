package com.personal.omnivault.controller;

import com.personal.omnivault.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final AwsS3Config s3Config;

    @GetMapping("/cloud-status")
    public ResponseEntity<Map<String, Boolean>> getCloudStorageStatus() {
        return ResponseEntity.ok(Map.of(
                "cloudStorageEnabled", s3Config.isEnabled()
        ));
    }
}