package com.werdnx.otus.socialnetwork.dto;

public record AuthResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        String tokenType
) {}

