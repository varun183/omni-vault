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

    /**
     * Finds a content item by its ID and associated user.
     *
     * @param id The unique identifier of the content
     * @param user The user who owns the content
     * @return An Optional containing the content if found, otherwise empty
     */
    Optional<Content> findByIdAndUser(UUID id, User user);

    /**
     * Retrieves all content items for a specific user with pagination.
     *
     * @param user The user whose content is being retrieved
     * @param pageable Pagination and sorting information
     * @return A page of content items belonging to the user
     */
    Page<Content> findAllByUser(User user, Pageable pageable);

    /**
     * Retrieves all content items in a specific folder for a user with pagination.
     *
     * @param user The user who owns the content
     * @param folder The folder containing the content
     * @param pageable Pagination and sorting information
     * @return A page of content items in the specified folder
     */
    Page<Content> findAllByUserAndFolder(User user, Folder folder, Pageable pageable);

    /**
     * Retrieves all content items of a specific type for a user with pagination.
     *
     * @param user The user who owns the content
     * @param contentType The type of content to retrieve
     * @param pageable Pagination and sorting information
     * @return A page of content items of the specified type
     */
    Page<Content> findAllByUserAndContentType(User user, ContentType contentType, Pageable pageable);

    /**
     * Retrieves all favorite content items for a user with pagination.
     *
     * @param user The user whose favorite content is being retrieved
     * @param pageable Pagination and sorting information
     * @return A page of favorite content items
     */
    Page<Content> findAllByUserAndFavoriteIsTrue(User user, Pageable pageable);

    /**
     * Retrieves all content items associated with a specific tag for a user.
     *
     * @param user The user who owns the content
     * @param tagId The unique identifier of the tag
     * @param pageable Pagination and sorting information
     * @return A page of content items with the specified tag
     */
    @Query("SELECT c FROM Content c JOIN c.tags t WHERE c.user = ?1 AND t.id = ?2")
    Page<Content> findAllByUserAndTagId(User user, UUID tagId, Pageable pageable);

    /**
     * Searches content items for a user based on a search term, matching against title and description.
     *
     * @param user The user whose content is being searched
     * @param searchTerm The term to search for
     * @param pageable Pagination and sorting information
     * @return A page of content items matching the search term
     */
    @Query("SELECT c FROM Content c WHERE c.user = ?1 AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<Content> searchContents(User user, String searchTerm, Pageable pageable);

    /**
     * Performs a comprehensive full-text search across content for a user.
     *
     * Searches through title, description, text content, and link URLs.
     *
     * @param userId The unique identifier of the user
     * @param searchTerm The term to search for
     * @param pageable Pagination and sorting information
     * @return A page of content items matching the search term across multiple fields
     */
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


    /**
     * Retrieves recently created content items for a user, sorted by creation date in descending order.
     *
     * @param user The user whose recent content is being retrieved
     * @param pageable Pagination and sorting information
     * @return A page of recently created content items
     */
    @Query("SELECT c FROM Content c WHERE c.user = ?1 ORDER BY c.createdAt DESC")
    Page<Content> findRecentContents(User user, Pageable pageable);

    /**
     * Retrieves the top 5 most viewed content items for a user, sorted by view count in descending order.
     *
     * @param user The user whose popular content is being retrieved
     * @return A list of the top 5 most viewed content items
     */
    List<Content> findTop5ByUserOrderByViewCountDesc(User user);
}