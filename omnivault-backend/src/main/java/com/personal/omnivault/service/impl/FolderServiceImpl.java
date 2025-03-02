package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.dto.request.FolderCreateRequest;
import com.personal.omnivault.domain.dto.response.FolderDTO;
import com.personal.omnivault.domain.model.Folder;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.FolderRepository;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "folders", key = "'rootFolders_' + @authService.getCurrentUser().getId()")
    public List<FolderDTO> getRootFolders() {
        User currentUser = authService.getCurrentUser();
        List<Folder> rootFolders = folderRepository.findAllByUserAndParentIsNull(currentUser);
        return rootFolders.stream()
                .map(this::convertToFolderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "folders", key = "'subFolders_' + #folderId")
    public List<FolderDTO> getSubfolders(UUID folderId) {
        User currentUser = authService.getCurrentUser();
        Folder parentFolder = folderRepository.findByIdAndUser(folderId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        List<Folder> subfolders = folderRepository.findAllByUserAndParentId(currentUser, folderId);
        return subfolders.stream()
                .map(this::convertToFolderDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "folders", key = "'folder_' + #folderId")
    public FolderDTO getFolder(UUID folderId) {
        return convertToFolderDto(getFolderEntity(folderId));
    }

    @Override
    public Folder getFolderEntity(UUID folderId) {
        User currentUser = authService.getCurrentUser();
        return folderRepository.findByIdAndUser(folderId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "folders", allEntries = true)
    public FolderDTO createFolder(FolderCreateRequest request) {
        User currentUser = authService.getCurrentUser();

        // Check for duplicate folder name at the same level
        if (request.getParentId() == null) {
            // Root level folder
            if (folderRepository.existsByNameAndParentIsNullAndUser(request.getName(), currentUser)) {
                throw new BadRequestException("A folder with this name already exists at the root level");
            }
        } else {
            // Subfolder
            if (folderRepository.existsByNameAndParentIdAndUser(request.getName(), request.getParentId(), currentUser)) {
                throw new BadRequestException("A folder with this name already exists in the parent folder");
            }

            // Verify parent folder exists and belongs to the user
            Folder parentFolder = folderRepository.findByIdAndUser(request.getParentId(), currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent folder", "id", request.getParentId()));
        }

        // Create new folder
        Folder folder = Folder.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(currentUser)
                .build();

        // Set parent if provided
        if (request.getParentId() != null) {
            Folder parentFolder = new Folder();
            parentFolder.setId(request.getParentId());
            folder.setParent(parentFolder);
        }

        Folder savedFolder = folderRepository.save(folder);
        log.info("Created new folder: {} for user: {}", savedFolder.getName(), currentUser.getUsername());

        return convertToFolderDto(savedFolder);
    }

    @Override
    @Transactional
    @CacheEvict(value = "folders", allEntries = true)
    public FolderDTO updateFolder(UUID folderId, FolderCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        Folder folder = folderRepository.findByIdAndUser(folderId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        // Check for duplicate folder name if name is changing
        if (!Objects.equals(folder.getName(), request.getName())) {
            if (folder.getParent() == null) {
                // Root level folder
                if (folderRepository.existsByNameAndParentIsNullAndUser(request.getName(), currentUser)) {
                    throw new BadRequestException("A folder with this name already exists at the root level");
                }
            } else {
                // Subfolder
                if (folderRepository.existsByNameAndParentIdAndUser(request.getName(), folder.getParent().getId(), currentUser)) {
                    throw new BadRequestException("A folder with this name already exists in the parent folder");
                }
            }
        }

        // Update folder properties
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());

        // Handle parent folder change if requested
        if (request.getParentId() != null &&
                (folder.getParent() == null || !folder.getParent().getId().equals(request.getParentId()))) {

            // Prevent circular references
            if (folder.getId().equals(request.getParentId())) {
                throw new BadRequestException("A folder cannot be its own parent");
            }

            // Check if new parent exists and belongs to user
            Folder newParent = folderRepository.findByIdAndUser(request.getParentId(), currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent folder", "id", request.getParentId()));

            // Check for duplicate name in new parent
            if (folderRepository.existsByNameAndParentIdAndUser(request.getName(), request.getParentId(), currentUser)) {
                throw new BadRequestException("A folder with this name already exists in the new parent folder");
            }

            folder.setParent(newParent);
        } else if (request.getParentId() == null && folder.getParent() != null) {
            // Moving to root level
            // Check for duplicate name at root level
            if (folderRepository.existsByNameAndParentIsNullAndUser(request.getName(), currentUser)) {
                throw new BadRequestException("A folder with this name already exists at the root level");
            }
            folder.setParent(null);
        }

        Folder updatedFolder = folderRepository.save(folder);
        log.info("Updated folder: {} for user: {}", updatedFolder.getName(), currentUser.getUsername());

        return convertToFolderDto(updatedFolder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"folders", "contents"}, allEntries = true)
    public void deleteFolder(UUID folderId) {
        User currentUser = authService.getCurrentUser();
        Folder folder = folderRepository.findByIdAndUser(folderId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        folderRepository.delete(folder);
        log.info("Deleted folder: {} for user: {}", folder.getName(), currentUser.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderDTO> searchFolders(String searchTerm) {
        User currentUser = authService.getCurrentUser();
        List<Folder> folders = folderRepository.searchFolders(currentUser, searchTerm);
        return folders.stream()
                .map(this::convertToFolderDto)
                .collect(Collectors.toList());
    }

    private FolderDTO convertToFolderDto(Folder folder) {
        int contentCount = folderRepository.countContentsByFolderId(folder.getId());
        int subfolderCount = folderRepository.countSubfoldersByFolderId(folder.getId());

        return FolderDTO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .path(folder.getPath())
                .contentCount(contentCount)
                .subfolderCount(subfolderCount)
                .subfolders(folder.getSubfolders() != null
                        ? folder.getSubfolders().stream()
                        .map(subfolder -> FolderDTO.builder()
                                .id(subfolder.getId())
                                .name(subfolder.getName())
                                .build())
                        .collect(Collectors.toList())
                        : Collections.emptyList()) // An empty list if subfolders is null
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}