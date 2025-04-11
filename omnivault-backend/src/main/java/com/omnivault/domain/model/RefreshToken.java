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
@Table(name = "refresh_tokens")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a refresh token for user authentication")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the refresh token",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id = null;

    @Column(nullable = false, unique = true)
    @Schema(
            description = "The actual refresh token string",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "The user associated with this refresh token")
    private User user;

    @Column(name = "expiry_date", nullable = false)
    @Schema(
            description = "The date and time when the refresh token expires",
            example = "2024-04-07T23:59:59+00:00"
    )
    private ZonedDateTime expiryDate;

    @Column(name = "is_blacklisted")
    @Schema(
            description = "Indicates whether the token has been blacklisted (invalidated)",
            example = "false"
    )
    private boolean blacklisted;

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the refresh token was created",
            example = "2024-04-07T12:34:56+00:00"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp when the refresh token was last updated",
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

    @Schema(description = "Checks if the refresh token has expired")
    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiryDate);
    }
}