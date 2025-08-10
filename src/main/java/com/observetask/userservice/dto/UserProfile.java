package com.observetask.userservice.dto;

public record UserProfile(
    String id,
    String email,
    String firstName,
    String lastName,
    String role,
    String organizationId,
    boolean isActive
) {
    public String fullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }
}