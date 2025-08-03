package com.werdnx.otus.socialnetwork.dto;

import java.time.Instant;

public record SendMessageRequest(
        Long peerId,
        String content
) {
}
