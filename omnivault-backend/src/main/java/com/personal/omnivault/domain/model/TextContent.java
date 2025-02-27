package com.personal.omnivault.domain.model;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextContent {

    @Id
    @Column(name = "content_id")
    private UUID contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "content_id")
    private Content content;

    @NotBlank
    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;
}