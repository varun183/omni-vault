package com.omnivault.service;

import com.omnivault.domain.model.Content;
import java.util.UUID;

/**
 * Service specifically for entity-level Content operations
 */
public interface ContentEntityService {

    /**
     * Get content entity by ID with ownership verification
     *
     * @param contentId The content ID
     * @return The content entity
     */
    Content getContentEntity(UUID contentId);
}