package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.dto.request.ContentUpdateRequest;
import com.personal.omnivault.domain.dto.request.LinkContentCreateRequest;
import com.personal.omnivault.domain.dto.request.TextContentCreateRequest;
import com.personal.omnivault.domain.dto.response.ContentDTO;
import com.personal.omnivault.domain.dto.response.TagDTO;
import com.personal.omnivault.domain.model.*;
import com.personal.omnivault.exception.AccessDeniedException;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.*;
import com.personal.omnivault.service.*;
import com.personal.omnivault.util.ContentTypeUtils;
import com.personal.omnivault.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final TextContentRepository textContentRepository;
    private final LinkContentRepository linkContentRepository;
    private final AuthService authService;
    private final FolderService folderService;
    private final TagService tagService;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contents", key = "'content_' +@authService.getCurrentUser().getId() + '_' + #contentId")
    public ContentDTO getContent(UUID contentId) {
        Content content = getContentEntity(contentId);

        // Asynchronously increment view count
        incrementViewCount(contentId);

        return convertToContentDto(content);
    }

    @Override
    @Transactional(readOnly = true)
    public Content getContentEntity(UUID contentId) {
        User currentUser = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Verify ownership
        if (!content.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this content");
        }

        return content;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contents", key = "'allContents_page_' + @authService.getCurrentUser().getId() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getAllContent(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Content> contentPage = contentRepository.findAllByUser(currentUser, pageable);

        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contentsByFolder", key = "'folder_' +@authService.getCurrentUser().getId() + '_' + #folderId + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getContentByFolder(UUID folderId, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Folder folder = folderService.getFolderEntity(folderId);

        // Verify ownership
        SecurityUtils.checkOwnership(folder.getUser(), currentUser, "Folder", folderId);

        Page<Content> contentPage = contentRepository.findAllByUserAndFolder(currentUser, folder, pageable);
        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contentsByType", key = "'type_' +@authService.getCurrentUser().getId() + '_' + #contentType + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getContentByType(ContentType contentType, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Content> contentPage = contentRepository.findAllByUserAndContentType(currentUser, contentType, pageable);

        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contentsByTag", key = "'tag_' +@authService.getCurrentUser().getId() + '_' + #tagId + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getContentByTag(UUID tagId, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Tag tag = tagService.getTagEntity(tagId);

        // Verify ownership
        SecurityUtils.checkOwnership(tag.getUser(), currentUser, "Tag", tagId);

        Page<Content> contentPage = contentRepository.findAllByUserAndTagId(currentUser, tagId, pageable);
        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contents", key = "'favorites_page_' +@authService.getCurrentUser().getId() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getFavoriteContent(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Content> contentPage = contentRepository.findAllByUserAndFavoriteIsTrue(currentUser, pageable);

        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "recentContents", key = "'recent_page_' +@authService.getCurrentUser().getId() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> getRecentContent(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Content> contentPage = contentRepository.findRecentContents(currentUser, pageable);

        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "popularContents")
    public List<ContentDTO> getPopularContent() {
        User currentUser = authService.getCurrentUser();
        List<Content> popularContent = contentRepository.findTop5ByUserOrderByViewCountDesc(currentUser);

        return popularContent.stream()
                .map(this::convertToContentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents", "recentContents", "popularContents","tags", "contentsByType"}, allEntries = true)
    public ContentDTO createTextContent(TextContentCreateRequest request) {
        User currentUser = authService.getCurrentUser();

        // Create content entity
        Content content = Content.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .contentType(ContentType.TEXT)
                .user(currentUser)
                .favorite(false)
                .viewCount(0)
                .metadata(request.getMetadata())
                .tags(new HashSet<>())
                .build();

        // Set folder if provided
        if (request.getFolderId() != null) {
            Folder folder = folderService.getFolderEntity(request.getFolderId());
            // Verify ownership
            SecurityUtils.checkOwnership(folder.getUser(), currentUser, "Folder", folder.getId());
            content.setFolder(folder);
        }

        // Save content first to get ID
        Content savedContent = contentRepository.save(content);

        // Create and save text content
        TextContent textContent = TextContent.builder()
                .contentId(savedContent.getId())
                .content(savedContent)
                .textContent(request.getTextContent())
                .build();

        textContentRepository.save(textContent);

        // Add tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = tagService.getTagsByIds(request.getTagIds());
            for (Tag tag : tags) {
                savedContent.addTag(tag);
            }
        }

        if (request.getNewTags() != null && !request.getNewTags().isEmpty()) {
            Set<Tag> newTags = tagService.findOrCreateTags(request.getNewTags());
            for (Tag tag : newTags) {
                savedContent.addTag(tag);
            }
        }

        // Save content again with tags
        savedContent = contentRepository.save(savedContent);

        log.info("Created new text content: {} for user: {}", savedContent.getTitle(), currentUser.getUsername());
        return convertToContentDto(savedContent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents", "recentContents", "popularContents","tags,", "contentsByType"}, allEntries = true)
    public ContentDTO createLinkContent(LinkContentCreateRequest request) {
        User currentUser = authService.getCurrentUser();

        // Create content entity
        Content content = Content.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .contentType(ContentType.LINK)
                .user(currentUser)
                .favorite(false)
                .viewCount(0)
                .metadata(request.getMetadata())
                .tags(new HashSet<>())
                .build();

        // Set folder if provided
        if (request.getFolderId() != null) {
            Folder folder = folderService.getFolderEntity(request.getFolderId());
            // Verify ownership
            SecurityUtils.checkOwnership(folder.getUser(), currentUser, "Folder", folder.getId());
            content.setFolder(folder);
        }

        // Save content first to get ID
        Content savedContent = contentRepository.save(content);

        // Create and save link content
        LinkContent linkContent = LinkContent.builder()
                .contentId(savedContent.getId())
                .content(savedContent)
                .url(request.getUrl())
                .build();

        linkContentRepository.save(linkContent);

        // Add tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = tagService.getTagsByIds(request.getTagIds());
            for (Tag tag : tags) {
                savedContent.addTag(tag);
            }
        }

        if (request.getNewTags() != null && !request.getNewTags().isEmpty()) {
            Set<Tag> newTags = tagService.findOrCreateTags(request.getNewTags());
            for (Tag tag : newTags) {
                savedContent.addTag(tag);
            }
        }

        // Save content again with tags
        savedContent = contentRepository.save(savedContent);

        log.info("Created new link content: {} for user: {}", savedContent.getTitle(), currentUser.getUsername());
        return convertToContentDto(savedContent);
    }



    @Override
    @Transactional
    @CacheEvict(value = {"contents", "recentContents", "popularContents","tags", "contentsByType"}, allEntries = true)
    public ContentDTO createFileContent(
            MultipartFile file,
            String title,
            String description,
            UUID folderId,
            List<UUID> tagIds,
            List<String> newTags
    ) {
        User currentUser = authService.getCurrentUser();

        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        // Determine content type based on file extension
        ContentType contentType = ContentTypeUtils.determineContentType(
                file.getOriginalFilename(),
                file.getContentType()
        );

        // Create content entity
        Content content = Content.builder()
                .title(StringUtils.hasText(title) ? title : file.getOriginalFilename())
                .description(description)
                .contentType(contentType)
                .user(currentUser)
                .favorite(false)
                .viewCount(0)
                .originalFilename(file.getOriginalFilename())
                .sizeBytes(file.getSize())
                .mimeType(file.getContentType())
                .tags(new HashSet<>())
                .build();

        // Set folder if provided
        if (folderId != null) {
            Folder folder = folderService.getFolderEntity(folderId);
            // Verify ownership
            SecurityUtils.checkOwnership(folder.getUser(), currentUser, "Folder", folder.getId());
            content.setFolder(folder);
        }

        // Store the file
        String storagePath = fileService.storeFile(file, currentUser.getId(), contentType);
        content.setStoragePath(storagePath);

        // Generate thumbnail for images and videos
        if (contentType == ContentType.IMAGE || contentType == ContentType.VIDEO) {
            String thumbnailPath = fileService.generateThumbnail(storagePath, contentType);
            content.setThumbnailPath(thumbnailPath);
        }

        // Save content
        Content savedContent = contentRepository.save(content);

        // Add tags if provided
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = tagService.getTagsByIds(tagIds);
            for (Tag tag : tags) {
                savedContent.addTag(tag);
            }
        }

        if (newTags != null && !newTags.isEmpty()) {
            Set<Tag> tags = tagService.findOrCreateTags(newTags);
            for (Tag tag : tags) {
                savedContent.addTag(tag);
            }
        }

        // Save content again with tags
        savedContent = contentRepository.save(savedContent);

        log.info("Created new file content: {} for user: {}", savedContent.getTitle(), currentUser.getUsername());
        return convertToContentDto(savedContent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents", "recentContents", "popularContents", "tags", "contentsByType"}, allEntries = true)
    public ContentDTO updateContent(UUID contentId, ContentUpdateRequest request) {
        Content content = getContentEntity(contentId);
        User currentUser = authService.getCurrentUser();

        // Update basic properties
        if (StringUtils.hasText(request.getTitle())) {
            content.setTitle(request.getTitle());
        }

        content.setDescription(request.getDescription());

        // Update folder if provided
        if (request.getFolderId() != null &&
                (content.getFolder() == null || !content.getFolder().getId().equals(request.getFolderId()))) {
            Folder folder = folderService.getFolderEntity(request.getFolderId());
            SecurityUtils.checkOwnership(folder.getUser(), currentUser, "Folder", folder.getId());
            content.setFolder(folder);
        } else if (request.getFolderId() == null && content.getFolder() != null) {
            content.setFolder(null);
        }

        // Update favorite status if provided
        if (request.getFavorite() != null) {
            content.setFavorite(request.getFavorite());
        }

        // Update metadata if provided
        if (request.getMetadata() != null) {
            content.setMetadata(request.getMetadata());
        }

        // Update tags if provided
        if ((request.getTagIds() != null && !request.getTagIds().isEmpty()) ||
                (request.getNewTags() != null && !request.getNewTags().isEmpty())) {

            // Clear existing tags safely - manually break bidirectional relationship
            Set<Tag> existingTags = new HashSet<>(content.getTags());
            for (Tag tag : existingTags) {
                content.removeTag(tag);
            }
            content.getTags().clear();

            // Add new tags
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                Set<Tag> tags = tagService.getTagsByIds(request.getTagIds());
                for (Tag tag : tags) {
                    content.addTag(tag);
                }
            }

            if (request.getNewTags() != null && !request.getNewTags().isEmpty()) {
                Set<Tag> tags = tagService.findOrCreateTags(request.getNewTags());
                for (Tag tag : tags) {
                    content.addTag(tag);
                }
            }
        }

        // Update content type specific properties
        if (content.getContentType() == ContentType.TEXT && StringUtils.hasText(request.getTextContent())) {
            TextContent textContent = textContentRepository.findByContentId(content.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("TextContent", "contentId", content.getId()));

            textContent.setTextContent(request.getTextContent());
            textContentRepository.save(textContent);
        } else if (content.getContentType() == ContentType.LINK && StringUtils.hasText(request.getUrl())) {
            LinkContent linkContent = linkContentRepository.findByContentId(content.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("LinkContent", "contentId", content.getId()));

            linkContent.setUrl(request.getUrl());
            linkContentRepository.save(linkContent);
        }

        Content updatedContent = contentRepository.save(content);
        log.info("Updated content: {} for user: {}", updatedContent.getTitle(), updatedContent.getUser().getUsername());

        return convertToContentDto(updatedContent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents",
            "recentContents",
            "popularContents",
            "contentsByFolder",
            "contentsByType"}, allEntries = true)
    public ContentDTO toggleFavorite(UUID contentId) {
        Content content = getContentEntity(contentId);
        content.setFavorite(!content.isFavorite());

        Content updatedContent = contentRepository.save(content);
        log.info("Toggled favorite for content: {} to {}", content.getTitle(), content.isFavorite());

        return convertToContentDto(updatedContent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents"}, allEntries = true)
    public ContentDTO updateContentTags(UUID contentId, List<UUID> tagIds, List<String> newTags) {
        Content content = getContentEntity(contentId);

        // Clear existing tags safely - manually break bidirectional relationship
        Set<Tag> existingTags = new HashSet<>(content.getTags());
        for (Tag tag : existingTags) {
            content.removeTag(tag);
        }
        content.getTags().clear();

        // Add new tags
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = tagService.getTagsByIds(tagIds);
            for (Tag tag : tags) {
                content.addTag(tag);
            }
        }

        if (newTags != null && !newTags.isEmpty()) {
            Set<Tag> tags = tagService.findOrCreateTags(newTags);
            for (Tag tag : tags) {
                content.addTag(tag);
            }
        }

        Content updatedContent = contentRepository.save(content);
        log.info("Updated tags for content: {}", content.getTitle());

        return convertToContentDto(updatedContent);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"contents", "recentContents", "popularContents", "tags", "contentsByType"}, allEntries = true)
    public void deleteContent(UUID contentId) {
        Content content = getContentEntity(contentId);

        // Delete file if it's a file-based content
        if (content.getStoragePath() != null) {
            fileService.deleteFile(content.getStoragePath());

        }

        contentRepository.delete(content);
        log.info("Deleted content: {} for user: {}", content.getTitle(), content.getUser().getUsername());
    }

    @Override
    public Resource getContentFile(UUID contentId) {
        Content content = getContentEntity(contentId);

        if (content.getStoragePath() == null) {
            throw new ResourceNotFoundException("File", "contentId", contentId);
        }

        return fileService.loadFileAsResource(content.getStoragePath());
    }

    @Override
    public Resource getContentThumbnail(UUID contentId) {
        Content content = getContentEntity(contentId);

        if (content.getThumbnailPath() == null) {
            throw new ResourceNotFoundException("Thumbnail", "contentId", contentId);
        }

        return fileService.loadFileAsResource(content.getThumbnailPath());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contents", key = "'search_' + #searchTerm + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContentDTO> searchContent(String searchTerm, Pageable pageable) {
        User currentUser = authService.getCurrentUser();

        // Sanitize search term to prevent SQL injection
        String sanitizedSearchTerm = searchTerm.replaceAll("[';\\\\%_]", "");

        Page<Content> contentPage = contentRepository.fullTextSearchContents(currentUser.getId(), sanitizedSearchTerm, pageable);

        return convertToContentDtoPage(contentPage);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID contentId) {
        contentRepository.findById(contentId).ifPresent(content -> {
            content.incrementViewCount();
            contentRepository.save(content);
        });
    }



    private ContentDTO convertToContentDto(Content content) {
        ContentDTO.ContentDTOBuilder builder = ContentDTO.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .contentType(content.getContentType())
                .folderId(content.getFolder() != null ? content.getFolder().getId() : null)
                .folderName(content.getFolder() != null ? content.getFolder().getName() : null)
                .sizeBytes(content.getSizeBytes())
                .mimeType(content.getMimeType())
                .storagePath(content.getStoragePath())
                .originalFilename(content.getOriginalFilename())
                .thumbnailPath(content.getThumbnailPath())
                .favorite(content.isFavorite())
                .viewCount(content.getViewCount())
                .metadata(content.getMetadata())
                .tags(content.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .color(tag.getColor())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt());

        // Add type-specific content
        if (content.getContentType() == ContentType.TEXT) {
            textContentRepository.findByContentId(content.getId()).ifPresent(textContent ->
                    builder.textContent(textContent.getTextContent()));
        } else if (content.getContentType() == ContentType.LINK) {
            linkContentRepository.findByContentId(content.getId()).ifPresent(linkContent -> {
                builder.url(linkContent.getUrl());
                builder.previewImagePath(linkContent.getPreviewImagePath());
            });
        }

        return builder.build();
    }

    private Page<ContentDTO> convertToContentDtoPage(Page<Content> contentPage) {
        List<ContentDTO> contentDtos = contentPage.getContent().stream()
                .map(this::convertToContentDto)
                .collect(Collectors.toList());

        return new PageImpl<>(contentDtos, contentPage.getPageable(), contentPage.getTotalElements());
    }
}