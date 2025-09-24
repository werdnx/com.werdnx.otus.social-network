package com.werdnx.otus.dialog.dto;

public record SendMessageRequest(
        Long peerId,
        String content
) {
}
