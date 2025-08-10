package com.werdnx.otus.socialnetwork.dto;

import java.time.Instant;

public record PostCreatedEvent(
        Long postId,
        Long authorId,
        String content,
        Instant createdAt
) { }
