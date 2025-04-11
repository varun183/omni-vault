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
@Table(name = "text_contents")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a text-based content item in the system")
public class TextContent {

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
    @Schema(description = "The parent Content entity associated with this text content")
    private Content content;

    @NotBlank
    @Column(name = "text_content", columnDefinition = "TEXT")
    @Schema(
            description = "The actual text content of the item",
            example = "This is a detailed note about project planning and requirements.",
            minLength = 1
    )
    private String textContent;

    @Version
    @Column(name = "version")
    @Schema(
            description = "Version number for optimistic locking",
            example = "0"
    )
    private Long version = 0L;
}