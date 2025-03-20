package com.personal.omnivault.service;

import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.model.User;

import java.util.UUID;

/**
 * Service for handling OAuth authentication flows
 */
public interface OAuthService {

    /**
     * Get the authorization URL for a given provider
     *
     * @param provider The OAuth provider (e.g., "google", "microsoft")
     * @return The authorization URL
     */
    String getAuthorizationUrl(String provider);

    /**
     * Handle the OAuth callback from a provider
     *
     * @param provider The OAuth provider
     * @param code The authorization code
     * @param state Optional state parameter for security
     * @return Authentication response with tokens
     */
    AuthResponse handleCallback(String provider, String code, String state);

    /**
     * Link an OAuth account to an existing user
     *
     * @param provider The OAuth provider
     * @param code The authorization code
     */
    void linkAccount(String provider, String code);

    /**
     * Unlink an OAuth account from a user
     *
     * @param provider The OAuth provider
     */
    void unlinkAccount(String provider);

    /**
     * Find a user by OAuth provider and ID
     *
     * @param provider The OAuth provider
     * @param providerUserId The user ID from the provider
     * @return The user, if found
     */
    User findUserByProviderAndId(String provider, String providerUserId);

    /**
     * Refresh OAuth tokens if needed
     *
     * @param provider The OAuth provider
     * @param userId The user ID
     * @return true if tokens were refreshed successfully
     */
    boolean refreshTokensIfNeeded(String provider, UUID userId);
}