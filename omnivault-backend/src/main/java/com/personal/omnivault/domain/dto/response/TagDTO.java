package com.personal.omnivault.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private UUID id;
    private String name;
    private String color;
    private int contentCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}