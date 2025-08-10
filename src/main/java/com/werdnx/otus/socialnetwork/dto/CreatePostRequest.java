package com.werdnx.otus.socialnetwork.dto;

public record CreatePostRequest(
        Long userId,
        String content
) { }
