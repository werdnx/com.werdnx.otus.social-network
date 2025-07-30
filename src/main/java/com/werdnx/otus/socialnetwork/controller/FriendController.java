package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {
    private final FriendRepository repo;

    @PostMapping("/add")
    public void add(@RequestParam Long userId, @RequestParam Long friendId) {
        repo.add(userId, friendId);
    }

    @DeleteMapping("/delete")
    public void del(@RequestParam Long userId, @RequestParam Long friendId) {
        repo.delete(userId, friendId);
    }
}

