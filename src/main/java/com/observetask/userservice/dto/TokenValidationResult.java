package com.observetask.userservice.dto;

public record TokenValidationResult(
    boolean valid,
    String userId,
    String organizationId,
    String role,
    String email,
    String errorMessage
) {
    // Factory methods
    public static TokenValidationResult valid(String userId, String organizationId, 
                                            String role, String email) {
        return new TokenValidationResult(true, userId, organizationId, role, email, null);
    }
    
    public static TokenValidationResult invalid(String errorMessage) {
        return new TokenValidationResult(false, null, null, null, null, errorMessage);
    }
}
