package com.personal.omnivault.service;

import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.request.TokenRefreshRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.model.User;

public interface AuthService {

    /**
     * Register a new user
     *
     * @param registerRequest The registration details
     * @return Authentication response with tokens
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * Authenticate a user
     *
     * @param loginRequest The login credentials
     * @return Authentication response with tokens
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Refresh an access token using a refresh token
     *
     * @param refreshRequest The refresh token request
     * @return Authentication response with new tokens
     */
    AuthResponse refreshToken(TokenRefreshRequest refreshRequest);

    /**
     * Logout a user
     *
     * @param refreshToken The refresh token to invalidate
     */
    void logout(String refreshToken);

    /**
     * Get the currently authenticated user
     *
     * @return The authenticated user
     */
    User getCurrentUser();

    /**
     * Verify email using token
     *
     * @param token The verification token
     * @return True if verification successful
     */
    boolean verifyEmail(String token);

    /**
     * Verify email using OTP code
     *
     * @param email The user's email
     * @param otpCode The OTP code
     * @return True if verification successful
     */
    boolean verifyEmailWithOTP(String email, String otpCode);

    /**
     * Resend verification email
     *
     * @param email The user's email
     * @return True if email sent successfully
     */
    boolean resendVerificationEmail(String email);
}