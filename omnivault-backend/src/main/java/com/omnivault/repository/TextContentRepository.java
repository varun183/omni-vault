package com.omnivault.repository;

import com.omnivault.domain.model.TextContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TextContentRepository extends JpaRepository<TextContent, UUID> {

    /**
     * Finds a text content entry by its associated content ID.
     *
     * @param contentId The unique identifier of the parent content
     * @return An Optional containing the TextContent if found, otherwise empty
     */
    Optional<TextContent> findByContentId(UUID contentId);
}