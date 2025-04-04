package com.personal.omnivault.util;

import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for security-related operations.
 * Provides methods for checking resource ownership and
 * performing security-related validations across the application.
 */
@Component
@Slf4j
public class SecurityUtils {


    public static void checkOwnership(UUID resourceOwnerId, UUID currentUserId, String resourceType, UUID resourceId) {
        if (!resourceOwnerId.equals(currentUserId)) {
            log.warn("Unauthorized access attempt to {} with ID: {} by user: {}", resourceType, resourceId, currentUserId);
            throw new AccessDeniedException("You don't have permission to access this " + resourceType.toLowerCase());
        }
    }


    public static void checkOwnership(User resourceOwner, User currentUser, String resourceType, UUID resourceId) {
        checkOwnership(resourceOwner.getId(), currentUser.getId(), resourceType, resourceId);
    }
}