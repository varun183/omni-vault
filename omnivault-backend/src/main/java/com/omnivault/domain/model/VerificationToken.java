package com.omnivault.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a verification token for user email verification")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the verification token",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id;

    @Column(nullable = false)
    @Schema(
            description = "The unique verification token string",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String token;

    @Column(name = "otp_code", length = 6)
    @Schema(
            description = "One-Time Password (OTP) for additional verification",
            example = "123456",
            minLength = 6,
            maxLength = 6
    )
    private String otpCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "The user associated with this verification token")
    private User user;

    @Column(name = "expiry_date", nullable = false)
    @Schema(
            description = "The date and time when the verification token expires",
            example = "2024-04-07T23:59:59+00:00"
    )
    private ZonedDateTime expiryDate;

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the verification token was created",
            example = "2024-04-07T12:34:56+00:00"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp when the verification token was last updated",
            example = "2024-04-07T12:34:56+00:00"
    )
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(
            description = "Version number for optimistic locking",
            example = "0"
    )
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    @Schema(description = "Checks if the verification token has expired")
    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiryDate);
    }
}