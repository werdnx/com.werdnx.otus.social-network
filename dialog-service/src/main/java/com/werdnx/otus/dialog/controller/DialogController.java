package com.werdnx.otus.dialog.controller;

import com.werdnx.otus.dialog.dto.MessageResponse;
import com.werdnx.otus.dialog.dto.SendMessageRequest;
import com.werdnx.otus.dialog.service.DialogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/dialogs/{userId}")
@RequiredArgsConstructor
public class DialogController {
    private final DialogService service;

    @PostMapping("/send")
    public ResponseEntity<Void> send(
            @PathVariable Long userId,
            @RequestBody SendMessageRequest dto
    ) {
        service.sendMessage(userId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<MessageResponse> list(
            @PathVariable Long userId,
            @RequestParam Long peerId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return service.getDialog(userId, peerId, limit);
    }
}
