package com.observetask.userservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.observetask.userservice.entity.RefreshToken;
import com.observetask.userservice.entity.User;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserId(UUID userId);

    void deleteByTokenHash(String tokenHash);

    void deleteByUserId(UUID userId);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt > CURRENT_TIMESTAMP AND rt.user.id = :userId")
    Long countActiveTokensByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.expiresAt > CURRENT_TIMESTAMP")
    Boolean existsValidToken(@Param("tokenHash") String tokenHash);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt BETWEEN CURRENT_TIMESTAMP AND :alertTime")
    List<RefreshToken> findTokensExpiringSoon(@Param("alertTime") LocalDateTime alertTime);

    @Query("SELECT rt from RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    List<RefreshToken> findExpiredTokens();

    @Modifying
    @Transactional
    
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

}
