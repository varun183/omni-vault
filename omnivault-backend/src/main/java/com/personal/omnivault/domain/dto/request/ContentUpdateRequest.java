package com.personal.omnivault.domain.dto.request;

import com.personal.omnivault.domain.model.StorageLocation;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentUpdateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private UUID folderId;

    private Boolean favorite;

    private List<UUID> tagIds;

    private List<String> newTags;

    private Map<String, Object> metadata;

    // Storage location preference
    private StorageLocation storageLocation;

    // For text content
    private String textContent;

    // For link content
    private String url;
}