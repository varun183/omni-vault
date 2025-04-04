package com.personal.omnivault.controller;

import com.personal.omnivault.config.RateLimiter;
import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.request.TokenRefreshRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.dto.response.UserDTO;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) TokenRefreshRequest request) {
        authService.logout(request != null ? request.getRefreshToken() : null);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserDTO());
    }

    @PostMapping("/verify/token")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@RequestParam String token) {
        boolean verified = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<Map<String, Boolean>> verifyEmailWithOTP(
            @RequestParam String email,
            @RequestParam String otpCode) {
        boolean verified = authService.verifyEmailWithOTP(email, otpCode);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Boolean>> resendVerificationEmail(
            @RequestParam String email,
            HttpServletRequest request) {

        // Check rate limit - 5 requests per hour
        if (!rateLimiter.allowRequest(request, 5)) {
            throw new BadRequestException("Too many verification requests. Please try again later.");
        }

        boolean sent = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("sent", sent));
    }
}