package com.omnivault.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "link_contents")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a link-based content item in the system")
public class LinkContent {

    @Id
    @Column(name = "content_id")
    @Schema(
            description = "The unique identifier of the content, which is the same as its parent Content entity",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "content_id")
    @Schema(description = "The parent Content entity associated with this link content")
    private Content content;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    @Schema(
            description = "The full URL of the link",
            example = "https://www.example.com/interesting-article",
            pattern = "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})[/\\w \\.-]*/?$"
    )
    private String url;

    @Column(name = "preview_image_path")
    @Schema(
            description = "Optional path to a preview image for the link",
            example = "/images/link-previews/example-preview.jpg"
    )
    private String previewImagePath;

    @Version
    @Column(name = "version")
    @Schema(
            description = "Version number for optimistic locking",
            example = "0"
    )
    private Long version = 0L;
}