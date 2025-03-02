package com.personal.omnivault.service;

import com.personal.omnivault.domain.dto.request.ContentUpdateRequest;
import com.personal.omnivault.domain.dto.request.LinkContentCreateRequest;
import com.personal.omnivault.domain.dto.request.TextContentCreateRequest;
import com.personal.omnivault.domain.dto.response.ContentDTO;
import com.personal.omnivault.domain.model.Content;
import com.personal.omnivault.domain.model.ContentType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ContentService {

    /**
     * Get content by ID
     *
     * @param contentId The content ID
     * @return The content DTO
     */
    ContentDTO getContent(UUID contentId);

    /**
     * Get content entity by ID
     *
     * @param contentId The content ID
     * @return The content entity
     */
    Content getContentEntity(UUID contentId);

    /**
     * Get all content with pagination
     *
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getAllContent(Pageable pageable);

    /**
     * Get content by folder
     *
     * @param folderId The folder ID
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getContentByFolder(UUID folderId, Pageable pageable);

    /**
     * Get content by type
     *
     * @param contentType The content type
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getContentByType(ContentType contentType, Pageable pageable);

    /**
     * Get content by tag
     *
     * @param tagId The tag ID
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getContentByTag(UUID tagId, Pageable pageable);

    /**
     * Get favorite content
     *
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getFavoriteContent(Pageable pageable);

    /**
     * Get recent content
     *
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> getRecentContent(Pageable pageable);

    /**
     * Get popular content
     *
     * @return List of popular content
     */
    List<ContentDTO> getPopularContent();

    /**
     * Create text content
     *
     * @param request The text content create request
     * @return The created content DTO
     */
    ContentDTO createTextContent(TextContentCreateRequest request);

    /**
     * Create link content
     *
     * @param request The link content create request
     * @return The created content DTO
     */
    ContentDTO createLinkContent(LinkContentCreateRequest request);

    /**
     * Create file content
     *
     * @param file The file to upload
     * @param title The content title
     * @param description The content description
     * @param folderId The folder ID
     * @param tagIds List of tag IDs
     * @return The created content DTO
     */
    ContentDTO createFileContent(
            MultipartFile file,
            String title,
            String description,
            UUID folderId,
            List<UUID> tagIds,
            List<String> newTags
    );

    /**
     * Update content
     *
     * @param contentId The content ID
     * @param request The content update request
     * @return The updated content DTO
     */
    ContentDTO updateContent(UUID contentId, ContentUpdateRequest request);

    /**
     * Toggle favorite status
     *
     * @param contentId The content ID
     * @return The updated content DTO
     */
    ContentDTO toggleFavorite(UUID contentId);

    /**
     * Update content tags
     *
     * @param contentId The content ID
     * @param tagIds List of tag IDs
     * @param newTags List of new tag names
     * @return The updated content DTO
     */
    ContentDTO updateContentTags(UUID contentId, List<UUID> tagIds, List<String> newTags);

    /**
     * Delete content
     *
     * @param contentId The content ID
     */
    void deleteContent(UUID contentId);

    /**
     * Get content file as resource
     *
     * @param contentId The content ID
     * @return The file resource
     */
    Resource getContentFile(UUID contentId);

    /**
     * Get content thumbnail as resource
     *
     * @param contentId The content ID
     * @return The thumbnail resource
     */
    Resource getContentThumbnail(UUID contentId);

    /**
     * Search content
     *
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of content
     */
    Page<ContentDTO> searchContent(String searchTerm, Pageable pageable);

    /**
     * Increment view count
     *
     * @param contentId The content ID
     */
    void incrementViewCount(UUID contentId);
}