package com.werdnx.otus.dialog.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.werdnx.otus.dialog.model.MessageRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MessageRedisRepository {

    private final StringRedisTemplate redis;
    @Qualifier("sendMessageScript")
    private final RedisScript<Long> sendMessageScript;
    @Qualifier("getDialogScript")
    private final RedisScript<String> getDialogScript;
    private final ObjectMapper objectMapper;

    public void save(MessageRecord message) {
        UUID cid = message.getConversationId();
        String seqKey = "dialog:%s:seq".formatted(cid);
        String listKey = "dialog:%s:messages".formatted(cid);
        long createdAt = (message.getCreatedAt() != null ? message.getCreatedAt() : Instant.now()).toEpochMilli();

        Long newId = redis.execute(
                sendMessageScript,
                List.of(seqKey, listKey),
                String.valueOf(message.getSenderId()),
                String.valueOf(message.getReceiverId()),
                message.getContent(),
                String.valueOf(createdAt)
        );
        if (newId != null) {
            message.setId(newId);
        }
    }

    public List<MessageRecord> findByConversation(UUID conversationId, int limit) {
        String listKey = "dialog:%s:messages".formatted(conversationId);
        String json = redis.execute(getDialogScript, List.of(listKey), String.valueOf(limit));
        if (json == null || json.isEmpty()) return List.of();

        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            List<MessageRecord> out = new ArrayList<>(raw.size());
            for (Map<String, Object> m : raw) {
                Long id = toLong(m.get("id"));
                Long senderId = toLong(m.get("senderId"));
                Long receiverId = toLong(m.get("receiverId"));
                String content = (String) m.get("content");
                Long createdAt = toLong(m.get("createdAt"));
                out.add(new MessageRecord(
                        id,
                        conversationId,
                        senderId,
                        receiverId,
                        content,
                        createdAt != null ? Instant.ofEpochMilli(createdAt) : null
                ));
            }
            return out;
        } catch (Exception e) {
            log.error("findByConversation",e);
            return List.of();
        }
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception ignore) { return null; }
    }
}
