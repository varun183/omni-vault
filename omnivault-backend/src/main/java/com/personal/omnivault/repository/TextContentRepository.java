package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.TextContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TextContentRepository extends JpaRepository<TextContent, UUID> {

    Optional<TextContent> findByContentId(UUID contentId);
}