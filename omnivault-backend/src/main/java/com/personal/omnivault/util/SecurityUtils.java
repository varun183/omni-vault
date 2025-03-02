package com.personal.omnivault.util;

import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
public class SecurityUtils {

    /**
     * Verifies that a resource belongs to the current user
     *
     * @param resourceOwnerId The owner ID of the resource
     * @param currentUserId   The current user's ID
     * @param resourceType    The type of resource being checked
     * @param resourceId      The ID of the resource being checked
     * @throws AccessDeniedException if the resource doesn't belong to the current user
     */
    public static void checkOwnership(UUID resourceOwnerId, UUID currentUserId, String resourceType, UUID resourceId) {
        if (!resourceOwnerId.equals(currentUserId)) {
            log.warn("Unauthorized access attempt to {} with ID: {} by user: {}", resourceType, resourceId, currentUserId);
            throw new AccessDeniedException("You don't have permission to access this " + resourceType.toLowerCase());
        }
    }

    /**
     * Verifies that a resource belongs to the current user
     *
     * @param resourceOwner The owner of the resource
     * @param currentUser   The current user
     * @param resourceType  The type of resource being checked
     * @param resourceId    The ID of the resource being checked
     * @throws AccessDeniedException if the resource doesn't belong to the current user
     */
    public static void checkOwnership(User resourceOwner, User currentUser, String resourceType, UUID resourceId) {
        checkOwnership(resourceOwner.getId(), currentUser.getId(), resourceType, resourceId);
    }
}