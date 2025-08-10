package com.observetask.userservice.repository;

import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.observetask.userservice.entity.Role;
import com.observetask.userservice.entity.UserRole;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole,UUID> {

    List<UserRole> findByUserId(UUID userId);
    List<UserRole> findByOrganizationId(UUID organizationId);
    Optional <UserRole> findByUserIdAndOrganizationId(UUID userId,UUID organizationId);
    Boolean existsByUserIdAndOrganizationId(UUID userId,UUID organizationId);
    List<UserRole> findByOrganizationIdAndRole(UUID OrganizationId,Role role);

    Long countByOrganizationIdAndRole(UUID organizationId,Role role);

    @Query("SELECT ur FROM UserRole ur WHERE ur.organizationId = :orgId AND ur.role IN :higherRoles")
    List<UserRole> findUsersWithHigherAuthority(@Param("orgId") UUID organizationId, @Param("higherRoles") List<Role> higherRoles);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.organizationId = :orgId")
    void deleteByUserIdAndOrganizationId(@Param("userId") UUID userId, @Param("orgId") UUID organizationId);

    @Query("SELECT DISTINCT ur.organizationId FROM UserRole ur WHERE ur.userId = :userId AND ur.role IN ('SUPER_ADMIN', 'ORG_ADMIN', 'TEAM_ADMIN')")
    List<UUID> findOrganizationsWhereUserIsAdmin(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
    

    

}
