package com.observetask.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles", schema = "observetask_users",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organization_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean canManageOrganization() {
        return role.hasAuthorityOver(Role.ORG_ADMIN);
    }

    public boolean canManageTeam() {
        return role.hasAuthorityOver(Role.TEAM_ADMIN);
    }

    public boolean canAssignRole(Role roleToAssign) {
        return role.canAssignRole(roleToAssign);
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }

    public boolean isOrgAdmin() {
        return role == Role.ORG_ADMIN;
    }

    public boolean isTeamAdmin() {
        return role == Role.TEAM_ADMIN;
    }

    public boolean isTeamMember() {
        return role == Role.TEAM_MEMBER;
    }
}