package com.personal.omnivault.controller;

import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/authorization-url/{provider}")
    public ResponseEntity<String> getAuthorizationUrl(@PathVariable String provider) {
        String authUrl = oAuthService.getAuthorizationUrl(provider);
        return ResponseEntity.ok(authUrl);
    }

    @GetMapping("/callback/{provider}")
    public ResponseEntity<AuthResponse> handleCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state) {

        AuthResponse authResponse = oAuthService.handleCallback(provider, code, state);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/link/{provider}")
    public ResponseEntity<Void> linkAccount(@PathVariable String provider, @RequestParam String code) {
        oAuthService.linkAccount(provider, code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlink/{provider}")
    public ResponseEntity<Void> unlinkAccount(@PathVariable String provider) {
        oAuthService.unlinkAccount(provider);
        return ResponseEntity.ok().build();
    }
}