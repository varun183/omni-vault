package com.omnivault.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag representation for categorizing content in OmniVault")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the tag",
            example = "550e8400-e29b-41d4-a716-446655440000",
            pattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    )
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Schema(
            description = "Name of the tag",
            example = "Important",
            minLength = 1,
            maxLength = 50
    )
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Column(columnDefinition = "varchar(7) default '#808080'")
    @Schema(
            description = "Hex color code for the tag",
            example = "#FF5733",
            pattern = "^#[0-9A-Fa-f]{6}$",
            defaultValue = "#808080"
    )
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @Schema(
            description = "User who created the tag",
            type = "object"
    )
    private User user;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Contents associated with this tag"
    )
    private Set<Content> contents = new HashSet<>();

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the tag was created",
            example = "2024-04-07T12:34:56.789Z",
            type = "string",
            format = "date-time"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp of the last tag update",
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

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
    }

    // Custom equals and hashCode implementation that doesn't rely on collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Safe getter for collections
    public Set<Content> getContents() {
        if (contents == null) {
            contents = new HashSet<>();
        }
        return contents;
    }
}