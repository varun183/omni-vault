package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.RefreshToken;
import com.personal.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = ?1")
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    void deleteAllExpiredTokens(ZonedDateTime now);

    List<RefreshToken> findAllByUserAndBlacklistedFalse(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.blacklisted = true WHERE rt.user = ?1")
    void blacklistAllUserTokens(User user);
}