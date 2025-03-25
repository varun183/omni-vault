package com.personal.omnivault.repository;

import com.personal.omnivault.domain.model.VerificationToken;
import com.personal.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByOtpCode(String otpCode);

    Optional<VerificationToken> findByUserAndOtpCodeIsNotNull(User user);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < ?1")
    void deleteAllExpiredTokens(ZonedDateTime now);

    void deleteAllByUser(User user);
}