package com.observetask.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "users", schema = "observetask_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = true, length = 255)
    private String passwordHash; // null for SSO users

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "external_id", length = 255)
    private String externalId; // SSO provider user ID

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // One user can have multiple roles across different organizations
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> roles = new ArrayList<>();

    // One user can have multiple refresh tokens (different devices)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addRole(UserRole role) {
        roles.add(role);
        role.setUser(this);
    }

    public void removeRole(UserRole role) {
        roles.remove(role);
        role.setUser(null);
    }

    // Authentication helper methods
    public boolean isLocalUser() {
        return authProvider == AuthProvider.LOCAL;
    }

    public boolean isSsoUser() {
        return authProvider != AuthProvider.LOCAL;
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.trim().isEmpty();
    }

    public boolean canLoginWithPassword() {
        return isLocalUser() && hasPassword() && isActive && emailVerified;
    }

    public boolean requiresPasswordReset() {
        return isLocalUser() && !hasPassword();
    }
}