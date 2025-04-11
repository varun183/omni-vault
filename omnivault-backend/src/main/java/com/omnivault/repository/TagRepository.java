package com.omnivault.repository;

import com.omnivault.domain.model.Tag;
import com.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Retrieves all tags for a specific user.
     *
     * @param user The user whose tags are being retrieved
     * @return A list of tags belonging to the user
     */
    List<Tag> findAllByUser(User user);

    /**
     * Finds a specific tag by its ID and associated user.
     *
     * @param id The unique identifier of the tag
     * @param user The user who owns the tag
     * @return An Optional containing the tag if found, otherwise empty
     */
    Optional<Tag> findByIdAndUser(UUID id, User user);

    /**
     * Finds a tag by its name for a specific user.
     *
     * @param name The name of the tag to find
     * @param user The user who owns the tag
     * @return An Optional containing the tag if found, otherwise empty
     */
    Optional<Tag> findByNameAndUser(String name, User user);

    /**
     * Finds multiple tags by their names for a specific user.
     *
     * @param names A list of tag names to search for
     * @param user The user who owns the tags
     * @return A set of tags matching the given names
     */
    @Query("SELECT t FROM Tag t WHERE t.name IN :names AND t.user = :user")
    Set<Tag> findByNameInAndUser(List<String> names, User user);

    /**
     * Finds multiple tags by their IDs for a specific user.
     *
     * @param ids A list of tag IDs to search for
     * @param user The user who owns the tags
     * @return A set of tags matching the given IDs
     */
    @Query("SELECT t FROM Tag t WHERE t.id IN :ids AND t.user = :user")
    Set<Tag> findByIdInAndUser(List<UUID> ids, User user);

    /**
     * Checks if a tag with the given name already exists for a user.
     *
     * @param name The name of the tag to check
     * @param user The user who owns the tag
     * @return true if a tag with the name exists, false otherwise
     */
    boolean existsByNameAndUser(String name, User user);

    /**
     * Searches for tags belonging to a user that match the given search term.
     * The search is case-insensitive and matches against tag names.
     *
     * @param user The user whose tags are being searched
     * @param searchTerm The term to search for in tag names
     * @return A list of tags matching the search criteria
     */
    @Query("SELECT t FROM Tag t WHERE t.user = ?1 AND LOWER(t.name) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Tag> searchTags(User user, String searchTerm);

    /**
     * Counts the number of content items associated with a specific tag.
     *
     * @param tagId The unique identifier of the tag
     * @return The total number of content items with this tag
     */
    @Query("SELECT COUNT(c) FROM Content c JOIN c.tags t WHERE t.id = ?1")
    int countContentsByTagId(UUID tagId);

    /**
     * Finds a tag by its ID and eagerly loads its associated user.
     * This method uses a left join fetch to avoid the N+1 query problem
     * when retrieving the user associated with the tag.
     *
     * @param id The unique identifier of the tag
     * @return An Optional containing the tag with its user, if found
     */
    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Tag> findByIdWithUser(UUID id);
}