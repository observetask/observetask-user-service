package com.observetask.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Principal class containing user information for JWT token generation
 * Used to pass user data to JwtUtils when creating access tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    
    /**
     * User's unique identifier
     */
    private UUID userId;
    
    /**
     * Organization ID the user belongs to
     * Used for multi-tenant data isolation
     */
    private UUID organizationId;
    
    /**
     * User's role within the organization
     * Values: SUPER_ADMIN, ORG_ADMIN, TEAM_ADMIN, TEAM_MEMBER
     */
    private String role;
    
    /**
     * User's email address
     */
    private String email;
    
    /**
     * User's first name
     */
    private String firstName;
    
    /**
     * User's last name
     */
    private String lastName;
    
    /**
     * Whether the user account is active
     */
    private boolean isActive;
    
    /**
     * Create UserPrincipal from User and UserRole entities
     * Convenience factory method
     */
    public static UserPrincipal from(com.observetask.userservice.entity.User user, 
                                   com.observetask.userservice.entity.UserRole userRole) {
        return UserPrincipal.builder()
                .userId(user.getId())
                .organizationId(userRole.getOrganizationId())
                .role(userRole.getRole().name())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .build();
    }
    
    /**
     * Get full name of the user
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email; // Fallback to email
    }
}