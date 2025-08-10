package com.observetask.userservice.entity;

public enum Role {
    SUPER_ADMIN("SUPER_ADMIN", "Global system administrator"),
    ORG_ADMIN("ORG_ADMIN", "Organization administrator"),
    TEAM_ADMIN("TEAM_ADMIN", "Team administrator"),
    TEAM_MEMBER("TEAM_MEMBER", "Team member");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this role has higher or equal authority than the given role
     * Lower ordinal = higher authority (SUPER_ADMIN=0 is highest)
     */
    public boolean hasAuthorityOver(Role other) {
        return this.ordinal() <= other.ordinal();
    }

    /**
     * Check if this role has authority over or equal to the minimum required role
     */
    public boolean hasMinimumAuthority(Role minimumRequired) {
        return this.ordinal() <= minimumRequired.ordinal();
    }

    /**
     * Check if this role can assign the given role
     */
    public boolean canAssignRole(Role roleToAssign) {
        switch (this) {
            case SUPER_ADMIN:
                return true; // Can assign any role
            case ORG_ADMIN:
                return roleToAssign != SUPER_ADMIN; // Can assign all except SUPER_ADMIN
            case TEAM_ADMIN:
                return roleToAssign == TEAM_MEMBER; // Can only assign TEAM_MEMBER
            case TEAM_MEMBER:
                return false; // Cannot assign any roles
            default:
                return false;
        }
    }

    /**
     * Check if this role can manage users with the given role
     */
    public boolean canManageRole(Role targetRole) {
        return hasAuthorityOver(targetRole);
    }

    /**
     * Get the minimum role required to perform organization management
     */
    public static Role getOrganizationManagementRole() {
        return ORG_ADMIN;
    }

    /**
     * Get the minimum role required to perform team management
     */
    public static Role getTeamManagementRole() {
        return TEAM_ADMIN;
    }
}