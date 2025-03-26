package com.personal.omnivault.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private final LoadingCache<String, Integer> requestCountsPerIpAddress;

    public RateLimiter() {
        requestCountsPerIpAddress = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(key -> 0);
    }

    public boolean allowRequest(HttpServletRequest request, int maxRequests) {
        String clientIpAddress = getClientIP(request);
        int count = requestCountsPerIpAddress.get(clientIpAddress);
        if (count >= maxRequests) {
            return false;
        }
        requestCountsPerIpAddress.put(clientIpAddress, count + 1);
        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}