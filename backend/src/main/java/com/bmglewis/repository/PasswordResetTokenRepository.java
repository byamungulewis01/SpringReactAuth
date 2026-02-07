package com.bmglewis.repository;

import com.bmglewis.model.PasswordResetToken;
import com.bmglewis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user")
    void deleteByUser(@Param("user") User user);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.email = :email AND prt.used = false AND prt.expiryDate > :now")
    List<PasswordResetToken> findValidTokensByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now OR prt.used = true")
    void deleteExpiredAndUsedTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user.email = :email AND prt.createdAt > :since")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") LocalDateTime since);
}