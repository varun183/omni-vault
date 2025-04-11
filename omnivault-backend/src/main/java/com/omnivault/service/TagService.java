package com.omnivault.service;

import com.omnivault.domain.dto.request.TagCreateRequest;
import com.omnivault.domain.dto.response.TagDTO;
import com.omnivault.domain.model.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing user tags.
 * Provides comprehensive tag management operations including
 * creation, retrieval, update, and deletion of tags. Supports
 * advanced tag-related functionalities like searching and
 * bulk tag operations.
 */
public interface TagService {

    /**
     * Get all tags for the current user
     *
     * @return List of tagsa
     */
    List<TagDTO> getAllTags();

    /**
     * Get tag by ID
     *
     * @param tagId The tag ID
     * @return The tag DTO
     */
    TagDTO getTag(UUID tagId);

    /**
     * Get tag entity by ID
     *
     * @param tagId The tag ID
     * @return The tag entity
     */
    Tag getTagEntity(UUID tagId);

    /**
     * Create a new tag
     *
     * @param request The tag creation request
     * @return The created tag DTO
     */
    TagDTO createTag(TagCreateRequest request);

    /**
     * Update a tag
     *
     * @param tagId The tag ID
     * @param request The tag update request
     * @return The updated tag DTO
     */
    TagDTO updateTag(UUID tagId, TagCreateRequest request);

    /**
     * Delete a tag
     *
     * @param tagId The tag ID
     */
    void deleteTag(UUID tagId);

    /**
     * Find or create tags by names
     *
     * @param tagNames List of tag names
     * @return Set of tag entities
     */
    Set<Tag> findOrCreateTags(List<String> tagNames);

    /**
     * Get tags by IDs
     *
     * @param tagIds List of tag IDs
     * @return Set of tag entities
     */
    Set<Tag> getTagsByIds(List<UUID> tagIds);

    /**
     * Search tags by name
     *
     * @param searchTerm The search term
     * @return List of matching tags
     */
    List<TagDTO> searchTags(String searchTerm);
}