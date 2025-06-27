package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.dto.AuthResponse;
import com.werdnx.otus.socialnetwork.model.User;
import com.werdnx.otus.socialnetwork.secutiry.JwtTokenProvider;
import com.werdnx.otus.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtProvider;

    public AuthController(UserService userService,
                          JwtTokenProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam Long id,
            @RequestParam String password
    ) {
        Optional<User> user = userService.validateCredentials(id, password);
        if (user.isEmpty()) {
            return ResponseEntity.status(401).build();
        } else {
            String accessToken = jwtProvider.createAccessToken(user.get().getId().toString());
            String refreshToken = jwtProvider.createRefreshToken(user.get().getId().toString());

            AuthResponse resp = new AuthResponse(
                    accessToken,
                    jwtProvider.getAccessTokenValiditySeconds(),
                    refreshToken,
                    "Bearer"
            );
            return ResponseEntity.ok(resp);
        }
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestParam String refreshToken
    ) {
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }
        String username = jwtProvider.getUsername(refreshToken);
        String newAccess = jwtProvider.createAccessToken(username);
        AuthResponse resp = new AuthResponse(
                newAccess,
                jwtProvider.getAccessTokenValiditySeconds(),
                refreshToken,
                "Bearer"
        );
        return ResponseEntity.ok(resp);
    }
}
