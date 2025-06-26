package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.model.User;
import com.werdnx.otus.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Long id = service.register(user);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }
}
