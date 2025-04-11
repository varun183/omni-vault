package com.omnivault.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Rate limiting component to prevent abuse of API endpoints.
 * Uses Caffeine cache to track and limit request counts per IP address.
 * Helps protect against potential denial-of-service attacks and excessive requests.
 */
@Component
public class RateLimiter {

    private final LoadingCache<String, Integer> requestCountsPerIpAddress;

    /**
     * Initializes the rate limiter with a cache configuration.
     * Configures the cache to:
     * - Expire entries after 1 hour of creation
     * - Initialize count to 0 for new IP addresses
     */
    public RateLimiter() {
        requestCountsPerIpAddress = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(key -> 0);
    }

    /**
     * Determines if a request should be allowed based on IP-based rate limiting.
     *
     * @param request The HTTP servlet request
     * @param maxRequests Maximum number of allowed requests
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    public boolean allowRequest(HttpServletRequest request, int maxRequests) {
        String clientIpAddress = getClientIP(request);
        int count = requestCountsPerIpAddress.get(clientIpAddress);
        if (count >= maxRequests) {
            return false;
        }
        requestCountsPerIpAddress.put(clientIpAddress, count + 1);
        return true;
    }

    /**
     * Retrieves the client's IP address, supporting proxied requests.
     * Checks for the X-Forwarded-For header to get the original client IP,
     * falling back to the remote address if not available.
     *
     * @param request The HTTP servlet request
     * @return The client's IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}