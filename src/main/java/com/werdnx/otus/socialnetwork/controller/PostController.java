package com.werdnx.otus.socialnetwork.controller;

import com.werdnx.otus.socialnetwork.model.Post;
import com.werdnx.otus.socialnetwork.repository.PostRepository;
import com.werdnx.otus.socialnetwork.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostRepository repo;
    private final FeedService feed;

    @PostMapping("/create")
    public Long create(@RequestBody Post p) {
        p.setCreatedAt(Instant.now());
        Long id = repo.create(p);
        feed.evictFollowersFeed(p.getUserId());
        return id;
    }

    @PutMapping("/update")
    public void update(@RequestBody Post p) {
        p.setCreatedAt(Instant.now());
        repo.update(p);
        feed.evictFollowersFeed(p.getUserId());
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam Long id) {
        Post p = repo.findById(id);
        repo.delete(id);
        feed.evictFollowersFeed(p.getUserId());
    }

    @GetMapping("/get")
    public Post get(@RequestParam Long id) {
        return repo.findById(id);
    }

    @GetMapping("/feed")
    public List<Post> feed(@RequestParam Long userId) {
        return feed.getFeed(userId);
    }
}

