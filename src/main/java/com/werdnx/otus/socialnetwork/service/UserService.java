package com.werdnx.otus.socialnetwork.service;

import com.werdnx.otus.socialnetwork.model.User;
import com.werdnx.otus.socialnetwork.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return repo.save(user);
    }

    public Optional<User> validateCredentials(Long id, String rawPassword) {
        User u = repo.findById(id);
        return encoder.matches(rawPassword, u.getPasswordHash()) ? Optional.of(u) : Optional.empty();
    }
    /**
     * Поиск по префиксам имени и фамилии
     */
    public List<User> searchByNamePrefixes(String firstNamePrefix, String lastNamePrefix) {
        return repo.searchByNamePrefixes(firstNamePrefix, lastNamePrefix);
    }
    public User get(Long id) {
        return repo.findById(id);
    }
}
