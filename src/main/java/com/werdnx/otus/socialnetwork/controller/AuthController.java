package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.dto.AuthResponse;
import com.werdnx.otus.socialnetwork.secutiry.JwtTokenProvider;
import com.werdnx.otus.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider jwtProvider) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam Long id,
            @RequestParam String password
    ) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(id, password)
        );
        String accessToken = jwtProvider.createAccessToken(auth.getName());
        String refreshToken = jwtProvider.createRefreshToken(auth.getName());

        AuthResponse resp = new AuthResponse(
                accessToken,
                jwtProvider.getAccessTokenValiditySeconds(),
                refreshToken,
                "Bearer"
        );
        return ResponseEntity.ok(resp);
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
