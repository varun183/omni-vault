package com.omnivault.service;

import com.omnivault.domain.model.User;
import com.omnivault.domain.model.VerificationToken;


/**
 * Service responsible for sending email communications.
 * Handles email verification processes, including sending initial
 * verification emails and resending verification tokens.
 */
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