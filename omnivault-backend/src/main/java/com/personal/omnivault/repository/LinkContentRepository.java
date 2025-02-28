package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.LinkContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkContentRepository extends JpaRepository<LinkContent, UUID> {

    Optional<LinkContent> findByContentId(UUID contentId);
}