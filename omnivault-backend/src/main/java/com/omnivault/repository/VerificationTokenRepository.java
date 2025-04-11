package com.omnivault.repository;

import com.omnivault.domain.model.VerificationToken;
import com.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /**
     * Finds a verification token by its unique token string.
     *
     * @param token The unique token string to search for
     * @return An Optional containing the VerificationToken if found, otherwise empty
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Finds a verification token by its OTP (One-Time Password) code.
     *
     * @param otpCode The OTP code to search for
     * @return An Optional containing the VerificationToken if found, otherwise empty
     */
    Optional<VerificationToken> findByOtpCode(String otpCode);

    /**
     * Finds an active OTP verification token for a specific user.
     *
     * @param user The user for whom to find the OTP verification token
     * @return An Optional containing the VerificationToken with an OTP if found, otherwise empty
     */
    Optional<VerificationToken> findByUserAndOtpCodeIsNotNull(User user);

    /**
     * Deletes all verification tokens that have expired as of the current time.
     * This method removes tokens that are past their expiration date.
     *
     * @param now The current time used to determine token expiration
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < ?1")
    void deleteAllExpiredTokens(ZonedDateTime now);

    /**
     * Deletes all verification tokens associated with a specific user.
     *
     * @param user The user whose verification tokens are to be deleted
     */
    void deleteAllByUser(User user);
}