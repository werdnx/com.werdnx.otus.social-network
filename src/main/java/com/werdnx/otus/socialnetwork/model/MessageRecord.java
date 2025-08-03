package com.werdnx.otus.socialnetwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecord {
    private Long id;
    private UUID conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant createdAt;
}
