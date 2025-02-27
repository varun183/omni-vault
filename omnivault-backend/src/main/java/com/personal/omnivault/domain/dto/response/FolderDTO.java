package com.personal.omnivault.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private String path;
    private int contentCount;
    private int subfolderCount;
    private List<FolderDTO> subfolders;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}