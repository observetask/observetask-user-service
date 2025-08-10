package com.observetask.userservice.dto;

public record ErrorResponse(
    String code,
    String message,
    String timestamp,
    String path
) {}