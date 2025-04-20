package com.omnivault.controller;

import com.omnivault.config.RateLimiter;
import com.omnivault.domain.dto.request.*;
import com.omnivault.domain.dto.response.AuthResponse;
import com.omnivault.domain.dto.response.UserDTO;
import com.omnivault.exception.BadRequestException;
import com.omnivault.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and sends a verification email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns authentication tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh token",
            description = "Generate new access token using a valid refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Logout",
            description = "Invalidates the user's refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) TokenRefreshRequest request) {
        authService.logout(request != null ? request.getRefreshToken() : null);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get current user",
            description = "Retrieves information about the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content)
    })
    @GetMapping("/user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserDTO());
    }

    @Operation(
            summary = "Verify email with token",
            description = "Verifies a user's email address using the token sent via email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verification successful",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content)
    })
    @PostMapping("/verify/token")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(
            @Parameter(description = "Verification token from email", required = true)
            @RequestParam String token) {
        boolean verified = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @Operation(
            summary = "Verify email with OTP",
            description = "Verifies a user's email address using an OTP (One-Time Password) code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verification successful",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP",
                    content = @Content)
    })
    @PostMapping("/verify/otp")
    public ResponseEntity<Map<String, Boolean>> verifyEmailWithOTP(
            @Parameter(description = "Email address", required = true)
            @RequestParam String email,
            @Parameter(description = "OTP code from email", required = true)
            @RequestParam String otpCode) {
        boolean verified = authService.verifyEmailWithOTP(email, otpCode);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @Operation(
            summary = "Resend verification email",
            description = "Resends the verification email with a new token and OTP"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Too many requests or invalid email",
                    content = @Content)
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Boolean>> resendVerificationEmail(
            @Parameter(description = "Email address", required = true)
            @RequestParam String email,
            HttpServletRequest request) {

        // Check rate limit - 5 requests per hour
        if (!rateLimiter.allowRequest(request, 5)) {
            throw new BadRequestException("Too many verification requests. Please try again later.");
        }

        boolean sent = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("sent", sent));
    }

    @Operation(
            summary = "Update user profile",
            description = "Allows authenticated user to update their profile information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @Operation(
            summary = "Change user password",
            description = "Allows authenticated user to change their password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete user account",
            description = "Allows authenticated user to permanently delete their account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)
    })
    @PostMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request) {
        authService.deleteAccount(request);
        return ResponseEntity.ok().build();
    }
}