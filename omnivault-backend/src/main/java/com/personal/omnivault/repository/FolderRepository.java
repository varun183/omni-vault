package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.Folder;
import com.personal.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findAllByUserAndParentIsNull(User user);

    List<Folder> findAllByUserAndParentId(User user, UUID parentId);

    Optional<Folder> findByIdAndUser(UUID id, User user);

    boolean existsByNameAndParentIdAndUser(String name, UUID parentId, User user);

    boolean existsByNameAndParentIsNullAndUser(String name, User user);

    @Query("SELECT f FROM Folder f WHERE f.user = ?1 AND (LOWER(f.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(f.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    List<Folder> searchFolders(User user, String searchTerm);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.folder.id = ?1")
    int countContentsByFolderId(UUID folderId);

    @Query("SELECT COUNT(f) FROM Folder f WHERE f.parent.id = ?1")
    int countSubfoldersByFolderId(UUID folderId);
}