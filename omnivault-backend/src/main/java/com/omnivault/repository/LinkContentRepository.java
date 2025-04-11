package com.omnivault.repository;

import com.omnivault.domain.model.LinkContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkContentRepository extends JpaRepository<LinkContent, UUID> {

    /**
     * Finds a link content entry by its associated content ID.
     *
     * @param contentId The unique identifier of the parent content
     * @return An Optional containing the LinkContent if found, otherwise empty
     */
    Optional<LinkContent> findByContentId(UUID contentId);
}