package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.Tag;
import com.personal.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAllByUser(User user);

    Optional<Tag> findByIdAndUser(UUID id, User user);

    Optional<Tag> findByNameAndUser(String name, User user);

    @Query("SELECT t FROM Tag t WHERE t.name IN :names AND t.user = :user")
    Set<Tag> findByNameInAndUser(List<String> names, User user);

    @Query("SELECT t FROM Tag t WHERE t.id IN :ids AND t.user = :user")
    Set<Tag> findByIdInAndUser(List<UUID> ids, User user);

    boolean existsByNameAndUser(String name, User user);

    @Query("SELECT t FROM Tag t WHERE t.user = ?1 AND LOWER(t.name) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Tag> searchTags(User user, String searchTerm);

    @Query("SELECT COUNT(c) FROM Content c JOIN c.tags t WHERE t.id = ?1")
    int countContentsByTagId(UUID tagId);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Tag> findByIdWithUser(UUID id);
}