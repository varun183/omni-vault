package com.personal.omnivault.service;

import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.domain.model.VerificationToken;

public interface EmailService {

    /**
     * Send verification email with link and OTP
     *
     * @param user The user to send verification email to
     * @param token The verification token
     * @param otpCode The OTP code
     */
    void sendVerificationEmail(User user, String token, String otpCode);

    /**
     * Resend verification email
     *
     * @param user The user to resend verification email
     * @return The new verification token
     */
    VerificationToken resendVerificationEmail(User user);
}