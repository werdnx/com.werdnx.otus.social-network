package com.werdnx.otus.socialnetwork.repository;

import com.werdnx.otus.socialnetwork.model.MessageRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<MessageRecord> rowMapper = (rs, rowNum) -> new MessageRecord(
            rs.getLong("id"),
            (UUID) rs.getObject("conversation_id"),
            rs.getLong("sender_id"),
            rs.getLong("receiver_id"),
            rs.getString("content"),
            rs.getTimestamp("created_at").toInstant()
    );

    public void save(MessageRecord message) {
        jdbc.update(
                "INSERT INTO messages(conversation_id, sender_id, receiver_id, content) VALUES (?, ?, ?, ?)",
                message.getConversationId(), message.getSenderId(), message.getReceiverId(), message.getContent()
        );
    }

    public List<MessageRecord> findByConversation(UUID conversationId, int limit) {
        return jdbc.query(
                "SELECT * FROM messages WHERE conversation_id = ? ORDER BY created_at DESC LIMIT ?",
                rowMapper, conversationId, limit
        );
    }
}
