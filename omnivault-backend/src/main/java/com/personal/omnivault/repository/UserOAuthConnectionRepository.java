package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.domain.model.UserOAuthConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOAuthConnectionRepository extends JpaRepository<UserOAuthConnection, UUID> {

    Optional<UserOAuthConnection> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<UserOAuthConnection> findByUserAndProvider(User user, String provider);

    boolean existsByUserAndProvider(User user, String provider);
}