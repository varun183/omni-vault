package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.dto.request.TagCreateRequest;
import com.personal.omnivault.domain.dto.response.TagDTO;
import com.personal.omnivault.domain.model.Tag;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.TagRepository;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.TagService;
import com.personal.omnivault.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final AuthService authService;

    private static final String DEFAULT_COLOR = "#808080";  // Default gray color

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'allTags_' + @authService.getCurrentUser().getId()")
    public List<TagDTO> getAllTags() {
        User currentUser = authService.getCurrentUser();
        List<Tag> tags = tagRepository.findAllByUser(currentUser);
        return tags.stream()
                .map(this::convertToTagDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'tag_' + #tagId")
    public TagDTO getTag(UUID tagId) {
        return convertToTagDto(getTagEntity(tagId));
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTagEntity(UUID tagId) {
        User currentUser = authService.getCurrentUser();
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));

        SecurityUtils.checkOwnership(tag.getUser(), currentUser, "Tag", tagId);

        return tag;
    }

    @Override
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagDTO createTag(TagCreateRequest request) {
        User currentUser = authService.getCurrentUser();

        // Check if tag with same name already exists for user
        if (tagRepository.existsByNameAndUser(request.getName(), currentUser)) {
            throw new BadRequestException("A tag with this name already exists");
        }

        // Create new tag
        Tag tag = Tag.builder()
                .name(request.getName())
                .color(StringUtils.hasText(request.getColor()) ? request.getColor() : DEFAULT_COLOR)
                .user(currentUser)
                .build();

        Tag savedTag = tagRepository.save(tag);
        log.info("Created new tag: {} for user: {}", savedTag.getName(), currentUser.getUsername());

        return convertToTagDto(savedTag);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagDTO updateTag(UUID tagId, TagCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));

        SecurityUtils.checkOwnership(tag.getUser(), currentUser, "Tag", tagId);

        // Check if name is changing and already exists
        if (!Objects.equals(tag.getName(), request.getName()) &&
                tagRepository.existsByNameAndUser(request.getName(), currentUser)) {
            throw new BadRequestException("A tag with this name already exists");
        }

        // Update tag
        tag.setName(request.getName());
        if (StringUtils.hasText(request.getColor())) {
            tag.setColor(request.getColor());
        }

        Tag updatedTag = tagRepository.save(tag);
        log.info("Updated tag: {} for user: {}", updatedTag.getName(), currentUser.getUsername());

        return convertToTagDto(updatedTag);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"tags", "contents"}, allEntries = true)
    public void deleteTag(UUID tagId) {
        User currentUser = authService.getCurrentUser();
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));

        SecurityUtils.checkOwnership(tag.getUser(), currentUser, "Tag", tagId);

        tagRepository.delete(tag);
        log.info("Deleted tag: {} for user: {}", tag.getName(), currentUser.getUsername());
    }

    @Override
    @Transactional
    public Set<Tag> findOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }

        User currentUser = authService.getCurrentUser();
        Set<Tag> result = new HashSet<>();

        // Find existing tags
        Set<Tag> existingTags = tagRepository.findByNameInAndUser(tagNames, currentUser);
        result.addAll(existingTags);

        // Create new tags for any that don't exist
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder()
                        .name(name)
                        .color(DEFAULT_COLOR)
                        .user(currentUser)
                        .build())
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            Iterable<Tag> savedTags = tagRepository.saveAll(newTags);
            savedTags.forEach(result::add);
            log.info("Created {} new tags for user: {}", newTags.size(), currentUser.getUsername());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Tag> getTagsByIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }

        User currentUser = authService.getCurrentUser();

        // Fetch tags individually and verify ownership to avoid circular reference issues
        Set<Tag> result = new HashSet<>();
        for (UUID tagId : tagIds) {
            try {
                Tag tag = tagRepository.findById(tagId).orElse(null);
                if (tag != null && tag.getUser().getId().equals(currentUser.getId())) {
                    result.add(tag);
                } else {
                    log.warn("Ignoring tag with ID {} as it doesn't exist or doesn't belong to user {}",
                            tagId, currentUser.getUsername());
                }
            } catch (Exception e) {
                log.warn("Error loading tag with ID {}: {}", tagId, e.getMessage());
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> searchTags(String searchTerm) {
        User currentUser = authService.getCurrentUser();
        List<Tag> tags = tagRepository.searchTags(currentUser, searchTerm);
        return tags.stream()
                .map(this::convertToTagDto)
                .collect(Collectors.toList());
    }

    private TagDTO convertToTagDto(Tag tag) {
        int contentCount = tagRepository.countContentsByTagId(tag.getId());

        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .contentCount(contentCount)
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}