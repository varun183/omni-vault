
package com.omnivault.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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
@Table(name = "folders", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "parent_id", "user_id"})
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Folder representation for organizing content in OmniVault")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the folder",
            example = "550e8400-e29b-41d4-a716-446655440000",
            pattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    )
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Schema(
            description = "Name of the folder",
            example = "Work Documents",
            minLength = 1,
            maxLength = 100
    )
    private String name;

    @Column(columnDefinition = "TEXT")
    @Schema(
            description = "Optional description of the folder",
            example = "Contains all work-related documents and files"
    )
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @Schema(
            description = "Parent folder, if this is a subfolder",
            type = "object"
    )
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Collection of subfolders within this folder"
    )
    private Set<Folder> subfolders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @Schema(
            description = "User who owns this folder",
            type = "object"
    )
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @Schema(
            description = "Contents stored in this folder"
    )
    private Set<Content> contents = new HashSet<>();

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the folder was created",
            example = "2024-04-07T12:34:56.789Z",
            type = "string",
            format = "date-time"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp of the last folder update",
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

    @Schema(
            description = "Checks if the folder is a root-level folder",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    public boolean isRoot() {
        return parent == null;
    }

    @Schema(
            description = "Gets the full path of the folder",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    public String getPath() {
        if (isRoot()) {
            return "/" + name;
        } else {
            return parent.getPath() + "/" + name;
        }
    }

    // Safe getters for collections
    public Set<Folder> getSubfolders() {
        if (subfolders == null) {
            subfolders = new HashSet<>();
        }
        return subfolders;
    }

    public Set<Content> getContents() {
        if (contents == null) {
            contents = new HashSet<>();
        }
        return contents;
    }

    // Custom equals and hashCode implementation that doesn't rely on collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return Objects.equals(id, folder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}