package com.werdnx.otus.socialnetwork.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        Instant createdAt
) {
}
