package com.werdnx.otus.socialnetwork.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private Long userId;
    private String content;
    private Instant createdAt;
}
