package com.personal.omnivault.domain.dto.response;

import com.personal.omnivault.domain.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private UUID id;
    private String title;
    private String description;
    private ContentType contentType;
    private UUID folderId;
    private String folderName;
    private Long sizeBytes;
    private String mimeType;
    private String storagePath;
    private String originalFilename;
    private String thumbnailPath;
    private boolean favorite;
    private Integer viewCount;
    private Map<String, Object> metadata;
    private List<TagDTO> tags;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // For text content
    private String textContent;

    // For link content
    private String url;
    private String previewImagePath;
}