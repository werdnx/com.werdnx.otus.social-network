package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam Long id, @RequestParam String password) {
        boolean ok = userService.validateCredentials(id, password);
        if (ok) return ResponseEntity.ok().build();
        else return ResponseEntity.status(401).build();
    }
}
