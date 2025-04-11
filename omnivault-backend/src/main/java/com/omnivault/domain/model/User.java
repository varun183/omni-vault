package com.omnivault.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User account representation in the OmniVault system")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the user",
            example = "550e8400-e29b-41d4-a716-446655440000",
            pattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    )
    private UUID id = null;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    @Schema(
            description = "Unique username for the user",
            example = "johndoe",
            minLength = 3,
            maxLength = 50
    )
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    @Column(unique = true)
    @Schema(
            description = "User's email address",
            example = "john.doe@example.com",
            format = "email"
    )
    private String email;

    @NotBlank
    @Size(max = 100)
    @JsonIgnore
    @Schema(
            description = "Hashed password for user authentication",
            example = "$2a$10$randomHashedPasswordString"
    )
    private String password;

    @Size(max = 50)
    @Column(name = "first_name")
    @Schema(
            description = "User's first name",
            example = "John",
            maxLength = 50
    )
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name")
    @Schema(
            description = "User's last name",
            example = "Doe",
            maxLength = 50
    )
    private String lastName;

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the user account was created",
            example = "2024-04-07T12:34:56.789Z",
            type = "string",
            format = "date-time"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp of the last account update",
            example = "2024-04-07T12:34:56.789Z",
            type = "string",
            format = "date-time"
    )
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(
            description = "Version number for optimistic locking",
            example = "0"
    )
    private Long version = 0L;

    @Column(name = "email_verified", nullable = false)
    @Schema(
            description = "Indicates whether the user's email has been verified",
            example = "false"
    )
    private boolean emailVerified = false;

    @Column(name = "enabled", nullable = false)
    @Schema(
            description = "Indicates whether the user account is active",
            example = "true"
    )
    private boolean enabled = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Collection of refresh tokens associated with the user"
    )
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Folders created by the user"
    )
    private Set<Folder> folders = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Tags created by the user"
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Contents created by the user"
    )
    private Set<Content> contents = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    // Custom equals and hashCode implementation that doesn't rely on collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Safe getters for collections
    public Set<RefreshToken> getRefreshTokens() {
        if (refreshTokens == null) {
            refreshTokens = new HashSet<>();
        }
        return refreshTokens;
    }

    public Set<Folder> getFolders() {
        if (folders == null) {
            folders = new HashSet<>();
        }
        return folders;
    }

    public Set<Tag> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public Set<Content> getContents() {
        if (contents == null) {
            contents = new HashSet<>();
        }
        return contents;
    }

}