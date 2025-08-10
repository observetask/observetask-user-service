package com.observetask.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
    @NotBlank @Email String email,
    @NotBlank String role,
    String organizationId,
    String teamId
) {}