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
import org.springframework.transaction.annotation.Transactional;

import com.observetask.userservice.entity.Invitation;
import com.observetask.userservice.entity.Invitation.InvitationStatus;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // ✅ AUTOMATIC METHODS (No @Query needed)
    
    /**
     * Find invitation by token
     */
    Optional<Invitation> findByToken(String token);
    
    /**
     * Find invitations by email address
     */
    List<Invitation> findByEmail(String email);
    
    /**
     * Find invitations for an organization
     */
    List<Invitation> findByOrganizationId(UUID organizationId);
    
    /**
     * Find invitations by who sent them
     */
    List<Invitation> findByInvitedBy(UUID invitedBy);
    
    /**
     * Find invitations by status
     */
    List<Invitation> findByStatus(InvitationStatus status);
    
    /**
     * Find invitations by organization and status
     */
    List<Invitation> findByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);
    
    /**
     * Count pending invitations per organization
     */
    Long countByOrganizationIdAndStatus(UUID organizationId, InvitationStatus status);
    
    /**
     * Check if invitation exists by email and organization (simple check)
     */
    Boolean existsByEmailAndOrganizationId(String email, UUID organizationId);
    
    /**
     * Find invitations by email and organization
     */
    List<Invitation> findByEmailAndOrganizationId(String email, UUID organizationId);

    // ❌ CUSTOM @Query METHODS (Complex logic)
    
    /**
     * Find pending invitations (not accepted/expired) - real-time expiration check
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP")
    List<Invitation> findPendingInvitations();
    
    /**
     * Find expired invitations (for cleanup) - exclude already marked as expired
     */
    @Query("SELECT i FROM Invitation i WHERE i.expiresAt < CURRENT_TIMESTAMP AND i.status != 'EXPIRED'")
    List<Invitation> findExpiredInvitations();
    
    /**
     * Check if email already has pending invitation to organization (business logic)
     */
    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.email = :email AND i.organizationId = :orgId AND i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP")
    Boolean existsPendingInvitationByEmailAndOrganization(@Param("email") String email, @Param("orgId") UUID organizationId);
    
    /**
     * Mark invitation as accepted (UPDATE operation)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Invitation i SET i.status = 'ACCEPTED', i.acceptedAt = CURRENT_TIMESTAMP WHERE i.id = :id")
    void markAsAccepted(@Param("id") UUID invitationId);
    
    /**
     * Mark invitation as expired (UPDATE operation)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED' WHERE i.id = :id")
    void markAsExpired(@Param("id") UUID invitationId);
    
    /**
     * Mark multiple invitations as expired by ID list
     */
    @Modifying
    @Transactional
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED' WHERE i.id IN :ids")
    void markMultipleAsExpired(@Param("ids") List<UUID> invitationIds);
    
    /**
     * Delete old invitations (cleanup) - only expired or revoked ones older than cutoff
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Invitation i WHERE i.expiresAt < :cutoffDate AND i.status IN ('EXPIRED', 'REVOKED')")
    Integer deleteOldInvitations(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find invitations expiring soon (for reminder emails)
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.expiresAt BETWEEN CURRENT_TIMESTAMP AND :reminderTime")
    List<Invitation> findInvitationsExpiringSoon(@Param("reminderTime") LocalDateTime reminderTime);
    
    /**
     * Find pending invitations for a specific organization with real-time expiration check
     */
    @Query("SELECT i FROM Invitation i WHERE i.organizationId = :orgId AND i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP")
    List<Invitation> findActivePendingInvitationsByOrganization(@Param("orgId") UUID organizationId);
    
    /**
     * Count active pending invitations for an organization
     */
    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.organizationId = :orgId AND i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP")
    Long countActivePendingInvitationsByOrganization(@Param("orgId") UUID organizationId);
    
    /**
     * Find invitations by email that are still actionable (pending and not expired)
     */
    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.status = 'PENDING' AND i.expiresAt > CURRENT_TIMESTAMP ORDER BY i.createdAt DESC")
    List<Invitation> findActionableInvitationsByEmail(@Param("email") String email);
    
    /**
     * Bulk expire invitations that have passed their expiration time
     */
    @Modifying
    @Transactional
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED' WHERE i.status = 'PENDING' AND i.expiresAt < CURRENT_TIMESTAMP")
    Integer expireOutdatedInvitations();
    
    /**
     * Find invitations by invited user with role filter
     */
    @Query("SELECT i FROM Invitation i WHERE i.invitedBy = :invitedBy AND i.role = :role ORDER BY i.createdAt DESC")
    List<Invitation> findByInvitedByAndRole(@Param("invitedBy") UUID invitedBy, @Param("role") com.observetask.userservice.entity.Role role);
}