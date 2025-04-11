package com.omnivault.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Storage locations for content")
public enum StorageLocation {
    @Schema(description = "Content stored locally on the server")
    LOCAL,

    @Schema(description = "Content stored in cloud storage (e.g., AWS S3)")
    CLOUD
}