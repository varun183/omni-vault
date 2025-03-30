package com.personal.omnivault.service.impl;

import com.personal.omnivault.config.EmailProperties;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.domain.model.VerificationToken;
import com.personal.omnivault.repository.VerificationTokenRepository;
import com.personal.omnivault.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.time.ZonedDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class SesEmailServiceImpl implements EmailService {

    private final SesClient sesClient;
    private final EmailProperties emailProperties;
    private final VerificationTokenRepository tokenRepository;
    private final Random random = new Random();

    @Value("${aws.ses.source}")
    private String sourceEmail;

    @Value("${aws.ses.reply-to:no-reply@omnivault.dev}")
    private String replyToEmail;

    @Async
    @Override
    public void sendVerificationEmail(User user, String token, String otpCode) {
        try {
            String verificationUrl = emailProperties.getVerification().getBaseUrl() + "?token=" + token;

            // Create the email content
            String htmlContent = createVerificationEmailContent(user.getFirstName(), verificationUrl, otpCode);

            // Create destination
            Destination destination = Destination.builder()
                    .toAddresses(user.getEmail())
                    .build();

            // Create message content
            Content subject = Content.builder()
                    .data("OmniVault - Verify Your Email")
                    .charset("UTF-8")
                    .build();

            Content htmlBody = Content.builder()
                    .data(htmlContent)
                    .charset("UTF-8")
                    .build();

            Body body = Body.builder()
                    .html(htmlBody)
                    .build();

            Message message = Message.builder()
                    .subject(subject)
                    .body(body)
                    .build();

            // Create send email request
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(sourceEmail)
                    .replyToAddresses(replyToEmail)
                    .destination(destination)
                    .message(message)
                    .build();

            // Send the email
            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            log.info("Verification email sent to: {}, message ID: {}", user.getEmail(), response.messageId());
        } catch (SesException e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public VerificationToken resendVerificationEmail(User user) {
        // Delete any existing tokens for this user
        tokenRepository.deleteAllByUser(user);

        // Generate new token and OTP
        String token = UUID.randomUUID().toString();
        String otpCode = generateOTP(emailProperties.getVerification().getOtpLength());

        // Calculate expiry dates
        ZonedDateTime tokenExpiryDate = ZonedDateTime.now()
                .plusMinutes(emailProperties.getVerification().getTokenExpiry());

        // Create and save new verification token
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .otpCode(otpCode)
                .user(user)
                .expiryDate(tokenExpiryDate)
                .build();

        VerificationToken savedToken = tokenRepository.save(verificationToken);

        // Send email
        sendVerificationEmail(user, token, otpCode);

        return savedToken;
    }

    private String createVerificationEmailContent(String firstName, String verificationUrl, String otpCode) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background-color: #0ea5e9; color: white; padding: 20px; text-align: center; }\n" +
                "        .content { padding: 20px; }\n" +
                "        .button { display: inline-block; padding: 10px 20px; background-color: #0ea5e9; color: white; text-decoration: none; border-radius: 5px; }\n" +
                "        .otp { background-color: #f0f9ff; padding: 10px; letter-spacing: 5px; font-size: 24px; font-weight: bold; text-align: center; margin: 20px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>OmniVault Email Verification</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Hello " + (firstName != null ? firstName : "there") + ",</p>\n" +
                "            <p>Thank you for registering with OmniVault. Please verify your email address by clicking the button below:</p>\n" +
                "            <p style=\"text-align: center;\">\n" +
                "                <a href=\"" + verificationUrl + "\" class=\"button\">Verify Email</a>\n" +
                "            </p>\n" +
                "            <p>If the button doesn't work, you can also use this verification code:</p>\n" +
                "            <div class=\"otp\">" + otpCode + "</div>\n" +
                "            <p>This verification code will expire in " + emailProperties.getVerification().getOtpExpiry() + " minutes.</p>\n" +
                "            <p>If you didn't create an account with OmniVault, please ignore this email.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}