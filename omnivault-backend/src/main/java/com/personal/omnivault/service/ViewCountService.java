package com.personal.omnivault.service;

import com.personal.omnivault.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountService {

    private final ContentRepository contentRepository;

    @Transactional
    public void incrementViewCount(UUID contentId) {
        contentRepository.findById(contentId).ifPresent(content -> {
            content.incrementViewCount();
            contentRepository.save(content);
        });
    }
}