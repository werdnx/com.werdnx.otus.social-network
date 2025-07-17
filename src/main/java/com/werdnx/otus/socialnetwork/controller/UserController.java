package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.model.User;
import com.werdnx.otus.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * GET /user/search?firstName=Adm&lastName=Us
     * Возвращает всех пользователей, first_name LIKE 'Adm%' AND last_name LIKE 'Us%', отсортированно по id
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> search(
            @RequestParam("firstName") String firstNamePrefix,
            @RequestParam("lastName") String lastNamePrefix
    ) {
        List<User> result = service.searchByNamePrefixes(firstNamePrefix, lastNamePrefix);
        return ResponseEntity.ok(result);
    }
}
