package com.werdnx.otus.socialnetwork.service;

import com.werdnx.otus.socialnetwork.model.User;
import com.werdnx.otus.socialnetwork.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Long register(User user) {
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        repo.save(user);
        // Retrieve ID based on business logic if needed
        return user.getId();
    }

    public Optional<User> validateCredentials(Long id, String rawPassword) {
        User u = repo.findById(id);
        return encoder.matches(rawPassword, u.getPasswordHash()) ? Optional.of(u) : Optional.empty();
    }

    public User get(Long id) {
        return repo.findById(id);
    }
}
