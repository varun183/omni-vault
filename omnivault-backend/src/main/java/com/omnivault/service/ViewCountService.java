package com.omnivault.service;

import com.omnivault.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

/**
 * Service responsible for managing content view count tracking.
 * Provides methods to increment the view count for content items,
 * helping track user engagement and content popularity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountService {

    private final ContentRepository contentRepository;

    /**
     * Increments the view count for a specific content item.
     * This method is transactional to ensure atomic update of the view count.
     * If the content is not found, no action is taken.
     *
     * @param contentId The unique identifier of the content to increment views for
     */
    @Transactional
    public void incrementViewCount(UUID contentId) {
        contentRepository.findById(contentId).ifPresent(content -> {
            content.incrementViewCount();
            contentRepository.save(content);
        });
    }
}