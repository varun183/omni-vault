package com.personal.omnivault.controller;

import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.request.TokenRefreshRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.dto.response.UserDTO;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.service.AuthService;
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
        User user = authService.getCurrentUser();
        UserDTO userDto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/verify/token")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@RequestParam String token) {
        boolean verified = authService.verifyEmail(token);
        Map<String, Boolean> response = Map.of("verified", verified);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<Map<String, Boolean>> verifyEmailWithOTP(
            @RequestParam String email,
            @RequestParam String otpCode) {
        boolean verified = authService.verifyEmailWithOTP(email, otpCode);
        Map<String, Boolean> response = Map.of("verified", verified);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Boolean>> resendVerificationEmail(
            @RequestParam String email) {
        boolean sent = authService.resendVerificationEmail(email);
        Map<String, Boolean> response = Map.of("sent", sent);
        return ResponseEntity.ok(response);
    }
}