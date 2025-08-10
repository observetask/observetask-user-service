package com.observetask.userservice.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    UserProfile user,
    long expiresIn
) {
    // Factory method for easy creation
    public static LoginResponse of(String accessToken, String refreshToken, 
                                 UserProfile user, long expiresIn) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", user, expiresIn);
    }
}