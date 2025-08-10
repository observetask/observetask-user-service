package com.observetask.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.observetask.userservice.entity.AuthProvider;
import com.observetask.userservice.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User,UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByExternalIdAndAuthProvider(String externalId, AuthProvider authProvider); 
    Boolean existsByEmail(String email);
    Boolean existsByExternalIdAndAuthProvider(String externalId,AuthProvider authProvider);
    List<User> findByIsActiveTrue();

    List<User> findByAuthProviderAndIsActiveTrue(AuthProvider authProvider);

    List<User> findByEmailVerifiedFalseAndIsActiveTrue();

    @Query("SELECT u FROM User u " +
           "WHERE u.authProvider = 'LOCAL' " +
           "AND (u.passwordHash IS NULL OR u.passwordHash = '') " +
           "AND u.isActive = true")
    List<User> findLocalUsersWithoutPassword();

    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.roles r " +
           "WHERE r.organizationId = :organizationId " +
           "AND u.isActive = true")
    List<User> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.roles r " +
           "WHERE r.organizationId = :organizationId " +
           "AND u.isActive = true")
    Page<User> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);

   
    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);

   
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
           "JOIN u.roles r " +
           "WHERE r.organizationId = :organizationId " +
           "AND u.isActive = true")
    long countActiveUsersByOrganization(@Param("organizationId") UUID organizationId);

   
    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND u.roles IS EMPTY")
    List<User> findUsersWithoutOrganization();

   
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") UUID userId);
    


}
