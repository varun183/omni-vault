package com.omnivault.repository;

import com.omnivault.domain.model.RefreshToken;
import com.omnivault.domain.model.User;
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

    /**
     * Finds a refresh token by its token string.
     *
     * @param token The unique token string to search for
     * @return An Optional containing the RefreshToken if found, otherwise empty
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with a specific user.
     * This method is modifying and will remove all refresh tokens for the given user.
     *
     * @param user The user whose refresh tokens are to be deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = ?1")
    void deleteByUser(User user);

    /**
     * Deletes all refresh tokens that have expired as of the current time.
     * This method is modifying and will remove tokens past their expiration date.
     *
     * @param now The current time used to determine token expiration
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    void deleteAllExpiredTokens(ZonedDateTime now);

    /**
     * Retrieves all non-blacklisted refresh tokens for a specific user.
     *
     * @param user The user whose active refresh tokens are being retrieved
     * @return A list of active (non-blacklisted) refresh tokens for the user
     */
    List<RefreshToken> findAllByUserAndBlacklistedFalse(User user);

    /**
     * Blacklists all refresh tokens for a specific user.
     * This method marks all refresh tokens for a user as blacklisted,
     * effectively invalidating them.
     *
     * @param user The user whose tokens are to be blacklisted
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.blacklisted = true WHERE rt.user = ?1")
    void blacklistAllUserTokens(User user);
}