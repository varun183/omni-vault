package com.personal.omnivault.domain.model;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkContent {

    @Id
    @Column(name = "content_id")
    private UUID contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "content_id")
    private Content content;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "preview_image_path")
    private String previewImagePath;

    @Version
    @Column(name = "version")
    private Long version = 0L;
}