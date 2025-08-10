package com.observetask.userservice.entity;

public enum AuthProvider {
    LOCAL("LOCAL", "Username/Password authentication"),
    AUTH0("AUTH0", "Auth0 SSO authentication"), 
    SAML("SAML", "SAML SSO authentication"),
    GOOGLE("GOOGLE", "Google OAuth authentication"),
    MICROSOFT("MICROSOFT", "Microsoft OAuth authentication");

    private final String code;
    private final String description;

    AuthProvider(String code, String description) {
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
     * Check if this provider requires local password storage
     */
    public boolean requiresLocalPassword() {
        return this == LOCAL;
    }

    /**
     * Check if this provider supports SSO
     */
    public boolean isSsoProvider() {
        return this != LOCAL;
    }

    /**
     * Check if this provider requires external ID mapping
     */
    public boolean requiresExternalId() {
        return isSsoProvider();
    }

    /**
     * Get the expected external ID format for validation
     */
    public String getExternalIdFormat() {
        switch (this) {
            case AUTH0:
                return "auth0|*"; // Auth0 user IDs typically start with "auth0|"
            case GOOGLE:
                return "google-oauth2|*"; // Google OAuth IDs
            case MICROSOFT:
                return "microsoft|*"; // Microsoft OAuth IDs
            case SAML:
                return "saml|*"; // SAML provider IDs
            case LOCAL:
                return null; // No external ID for local users
            default:
                return "unknown|*";
        }
    }
}