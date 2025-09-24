package com.werdnx.otus.dialog.service;

import com.werdnx.otus.dialog.dto.MessageResponse;
import com.werdnx.otus.dialog.dto.SendMessageRequest;
import com.werdnx.otus.dialog.model.MessageRecord;
import com.werdnx.otus.dialog.repository.MessageRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogService {
    private final MessageRedisRepository messageDao;

    public void sendMessage(Long userId, SendMessageRequest dto) {
        UUID conversationId = generateConversationId(userId, dto.peerId());
        MessageRecord record = new MessageRecord(null,
                conversationId,
                userId,
                dto.peerId(),
                dto.content(),
                Instant.now()
        );
        messageDao.save(record);
        log.info("record {} saved", record);
    }

    public List<MessageResponse> getDialog(Long userId, Long peerId, int limit) {
        UUID conversationId = generateConversationId(userId, peerId);
        return messageDao.findByConversation(conversationId, limit)
                .stream()
                .map(r -> new MessageResponse(r.getId(), r.getSenderId(), r.getReceiverId(), r.getContent(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Генерация одного UUID для пары пользователей (константный порядок)
    private UUID generateConversationId(Long a, Long b) {
        long low = Math.min(a, b);
        long high = Math.max(a, b);
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putLong(low);
        buf.putLong(high);
        buf.flip();
        return UUID.nameUUIDFromBytes(buf.array());
    }
}
