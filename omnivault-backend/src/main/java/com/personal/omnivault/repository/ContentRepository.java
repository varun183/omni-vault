package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.Content;
import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.domain.model.Folder;
import com.personal.omnivault.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

    Optional<Content> findByIdAndUser(UUID id, User user);

    Page<Content> findAllByUser(User user, Pageable pageable);

    Page<Content> findAllByUserAndFolder(User user, Folder folder, Pageable pageable);

    Page<Content> findAllByUserAndContentType(User user, ContentType contentType, Pageable pageable);

    Page<Content> findAllByUserAndFavoriteIsTrue(User user, Pageable pageable);

    @Query("SELECT c FROM Content c JOIN c.tags t WHERE c.user = ?1 AND t.id = ?2")
    Page<Content> findAllByUserAndTagId(User user, UUID tagId, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.user = ?1 AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Content> searchContents(User user, String searchTerm, Pageable pageable);

    // Using a parameterized query to avoid issues with sorting in native queries
    @Query("SELECT c FROM Content c " +
            "LEFT JOIN TextContent tc ON c.id = tc.content.id " +
            "LEFT JOIN LinkContent lc ON c.id = lc.content.id " +
            "WHERE c.user.id = :userId AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(COALESCE(tc.textContent, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(COALESCE(lc.url, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Content> fullTextSearchContents(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT c FROM Content c WHERE c.user = ?1 ORDER BY c.createdAt DESC")
    Page<Content> findRecentContents(User user, Pageable pageable);

    List<Content> findTop5ByUserOrderByViewCountDesc(User user);
}