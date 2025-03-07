package com.personal.omnivault.domain.model;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id = null;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private ZonedDateTime expiryDate;

    @Column(name = "is_blacklisted")
    private boolean blacklisted;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiryDate);
    }
}