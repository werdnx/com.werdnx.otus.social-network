package com.werdnx.otus.dialog.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        Instant createdAt
) {
}
