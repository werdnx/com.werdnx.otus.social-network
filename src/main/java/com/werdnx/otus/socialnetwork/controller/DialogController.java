package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.client.DialogClient;
import com.werdnx.otus.socialnetwork.dto.MessageResponse;
import com.werdnx.otus.socialnetwork.dto.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Deprecated
@RestController
@RequestMapping("/dialog/{userId}")
@RequiredArgsConstructor
public class DialogController {
    private final DialogClient dialogClient;

    @PostMapping("/send")
    public ResponseEntity<Void> send(
            @PathVariable Long userId,
            @RequestBody SendMessageRequest dto,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        dialogClient.sendV2(userId, dto, authorization);
        log.info("message {} saved", dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<MessageResponse> list(
            @PathVariable Long userId,
            @RequestParam Long peerId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return dialogClient.listV2(userId, peerId, limit, authorization);
    }
}
