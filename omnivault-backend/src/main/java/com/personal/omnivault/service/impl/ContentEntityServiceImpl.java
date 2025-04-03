package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.model.Content;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.AccessDeniedException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.ContentRepository;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.ContentEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentEntityServiceImpl implements ContentEntityService {

    private final ContentRepository contentRepository;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public Content getContentEntity(UUID contentId) {
        User currentUser = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        // Verify ownership
        if (!content.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this content");
        }

        return content;
    }
}