package com.personal.omnivault.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.omnivault.config.JwtProperties;
import com.personal.omnivault.config.OAuthProperties;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.dto.response.UserDTO;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.domain.model.UserOAuthConnection;
import com.personal.omnivault.exception.AuthenticationException;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.repository.UserOAuthConnectionRepository;
import com.personal.omnivault.repository.UserRepository;
import com.personal.omnivault.security.TokenProvider;
import com.personal.omnivault.security.UserPrincipal;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthServiceImpl implements OAuthService {

    private final OAuthProperties oAuthProperties;
    private final UserRepository userRepository;
    private final UserOAuthConnectionRepository oAuthConnectionRepository;
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getAuthorizationUrl(String provider) {
        OAuthProperties.ProviderConfig providerConfig = getProviderConfig(provider);

        return providerConfig.getAuthorizationUri() + "?client_id=" + providerConfig.getClientId() +
                "&redirect_uri=" + providerConfig.getRedirectUri() +
                "&response_type=code" +
                "&scope=" + String.join(" ", providerConfig.getScopes());
    }

    @Override
    @Transactional
    public AuthResponse handleCallback(String provider, String code, String state) {
        OAuthProperties.ProviderConfig providerConfig = getProviderConfig(provider);

        // Exchange code for tokens
        JsonNode tokenResponse = exchangeCodeForTokens(provider, code);

        // Get user info from provider
        JsonNode userInfo = getUserInfo(provider, tokenResponse.get("access_token").asText());

        // Extract provider user ID and email
        String providerUserId = extractProviderUserId(provider, userInfo);
        String email = userInfo.get("email").asText();

        // Find or create user
        User user = findUserByProviderAndId(provider, providerUserId);
        if (user == null) {
            // Check if user exists with the same email
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                // Link to existing account
                user = existingUser.get();
                createOrUpdateOAuthConnection(user, provider, providerUserId, userInfo, tokenResponse);
            } else {
                // Create new user
                user = createUserFromOAuth(provider, userInfo, tokenResponse);
            }
        } else {
            // Update existing OAuth connection
            createOrUpdateOAuthConnection(user, provider, providerUserId, userInfo, tokenResponse);
        }

        // Create authentication
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(UUID.randomUUID().toString()) // This should be a proper refresh token
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(convertToUserDto(user))
                .build();
    }

    @Override
    @Transactional
    public void linkAccount(String provider, String code) {
        // Get current authenticated user
        User currentUser = authService.getCurrentUser();

        // Exchange code for tokens
        JsonNode tokenResponse = exchangeCodeForTokens(provider, code);

        // Get user info from provider
        JsonNode userInfo = getUserInfo(provider, tokenResponse.get("access_token").asText());

        // Extract provider user ID
        String providerUserId = extractProviderUserId(provider, userInfo);

        // Check if this OAuth account is already linked to another user
        User existingLinkedUser = findUserByProviderAndId(provider, providerUserId);
        if (existingLinkedUser != null && !existingLinkedUser.getId().equals(currentUser.getId())) {
            throw new BadRequestException("This " + provider + " account is already linked to another user");
        }

        // Create or update OAuth connection
        createOrUpdateOAuthConnection(currentUser, provider, providerUserId, userInfo, tokenResponse);
    }

    @Override
    @Transactional
    public void unlinkAccount(String provider) {
        User currentUser = authService.getCurrentUser();

        // Find and remove the OAuth connection
        Optional<UserOAuthConnection> connection = oAuthConnectionRepository
                .findByUserAndProvider(currentUser, provider);

        connection.ifPresent(oAuthConnectionRepository::delete);
    }

    @Override
    public User findUserByProviderAndId(String provider, String providerUserId) {
        Optional<UserOAuthConnection> connection = oAuthConnectionRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        return connection.map(UserOAuthConnection::getUser).orElse(null);
    }

    @Override
    @Transactional
    public boolean refreshTokensIfNeeded(String provider, UUID userId) {
        // Get user and connection
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        Optional<UserOAuthConnection> connectionOpt = oAuthConnectionRepository
                .findByUserAndProvider(user, provider);

        if (connectionOpt.isEmpty()) {
            return false;
        }

        UserOAuthConnection connection = connectionOpt.get();

        // Check if tokens need refresh
        if (connection.getTokenExpiresAt() == null ||
                connection.getTokenExpiresAt().isAfter(ZonedDateTime.now().plusMinutes(5))) {
            // Still valid for at least 5 more minutes
            return true;
        }

        // Refresh tokens
        OAuthProperties.ProviderConfig providerConfig = getProviderConfig(provider);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", providerConfig.getClientId());
            body.add("client_secret", providerConfig.getClientSecret());
            body.add("refresh_token", connection.getRefreshToken());
            body.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    providerConfig.getTokenUri(),
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode tokenResponse = objectMapper.readTree(response.getBody());

                // Update access token
                connection.setAccessToken(tokenResponse.get("access_token").asText());

                // Update refresh token if provided
                if (tokenResponse.has("refresh_token")) {
                    connection.setRefreshToken(tokenResponse.get("refresh_token").asText());
                }

                // Update expiry
                if (tokenResponse.has("expires_in")) {
                    connection.setTokenExpiresAt(
                            ZonedDateTime.now().plusSeconds(tokenResponse.get("expires_in").asLong())
                    );
                }

                oAuthConnectionRepository.save(connection);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to refresh OAuth tokens for user {} and provider {}", userId, provider, e);
            return false;
        }
    }

    // Helper methods

    private JsonNode exchangeCodeForTokens(String provider, String code) {
        OAuthProperties.ProviderConfig providerConfig = getProviderConfig(provider);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", providerConfig.getClientId());
            body.add("client_secret", providerConfig.getClientSecret());
            body.add("code", code);
            body.add("redirect_uri", providerConfig.getRedirectUri());
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    providerConfig.getTokenUri(),
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }

            throw new AuthenticationException("Failed to exchange code for tokens: " + response.getBody());
        } catch (Exception e) {
            log.error("Error exchanging code for tokens", e);
            throw new AuthenticationException("Failed to authenticate with " + provider);
        }
    }

    private JsonNode getUserInfo(String provider, String accessToken) {
        OAuthProperties.ProviderConfig providerConfig = getProviderConfig(provider);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    providerConfig.getUserInfoUri(),
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }

            throw new AuthenticationException("Failed to get user info: " + response.getBody());
        } catch (Exception e) {
            log.error("Error getting user info", e);
            throw new AuthenticationException("Failed to get user info from " + provider);
        }
    }

    private String extractProviderUserId(String provider, JsonNode userInfo) {
        return switch (provider.toLowerCase()) {
            case "google" -> userInfo.get("sub").asText();
            case "microsoft" -> userInfo.get("id").asText();
            default -> throw new BadRequestException("Unsupported OAuth provider: " + provider);
        };
    }

    private User createUserFromOAuth(String provider, JsonNode userInfo, JsonNode tokenResponse) {
        String providerUserId = extractProviderUserId(provider, userInfo);
        String email = userInfo.get("email").asText();
        String name = userInfo.has("name") ? userInfo.get("name").asText() : "";

        // Split name into first and last name
        String firstName = name;
        String lastName = "";
        if (name.contains(" ")) {
            String[] nameParts = name.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        }

        // Create username from email
        String username = email.split("@")[0] + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Create user
        User user = User.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        user = userRepository.save(user);

        // Create OAuth connection
        createOrUpdateOAuthConnection(user, provider, providerUserId, userInfo, tokenResponse);

        return user;
    }

    private void createOrUpdateOAuthConnection(User user, String provider, String providerUserId,
                                               JsonNode userInfo, JsonNode tokenResponse) {
        // Look for existing connection
        Optional<UserOAuthConnection> existingConnection = oAuthConnectionRepository
                .findByUserAndProvider(user, provider);

        UserOAuthConnection connection = existingConnection.orElse(
                UserOAuthConnection.builder()
                        .user(user)
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .build()
        );

        // Update connection
        connection.setEmail(userInfo.get("email").asText());
        connection.setDisplayName(userInfo.has("name") ? userInfo.get("name").asText() : "");
        connection.setAccessToken(tokenResponse.get("access_token").asText());

        if (tokenResponse.has("refresh_token")) {
            connection.setRefreshToken(tokenResponse.get("refresh_token").asText());
        }

        if (tokenResponse.has("expires_in")) {
            connection.setTokenExpiresAt(
                    ZonedDateTime.now().plusSeconds(tokenResponse.get("expires_in").asLong())
            );
        }

        // Save scopes
        if (tokenResponse.has("scope")) {
            connection.setScopes(tokenResponse.get("scope").asText());
        }

        oAuthConnectionRepository.save(connection);
    }

    private OAuthProperties.ProviderConfig getProviderConfig(String provider) {
        OAuthProperties.ProviderConfig config = oAuthProperties.getProviders().get(provider.toLowerCase());
        if (config == null) {
            throw new BadRequestException("Unsupported OAuth provider: " + provider);
        }
        return config;
    }

    private UserDTO convertToUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}