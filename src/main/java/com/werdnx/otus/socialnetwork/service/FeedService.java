package com.werdnx.otus.socialnetwork.service;

import com.werdnx.otus.socialnetwork.model.Post;
import com.werdnx.otus.socialnetwork.repository.FriendRepository;
import com.werdnx.otus.socialnetwork.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private static final int MAX_FEED = 1_000;

    private final FriendRepository friends;
    private final PostRepository posts;
    private final CacheManager cm;

    // на GET /post/feed
    @Cacheable(cacheNames="feed", key="#userId")
    public List<Post> getFeed(Long userId) {
        List<Long> fids = friends.findFriends(userId);
        return posts.findRecentByUsers(fids, MAX_FEED);
    }

    // вызываем в create/update/delete поста
    public void evictFollowersFeed(Long authorId) {
        List<Long> followers = friends.findFollowers(authorId);
        Cache cache = cm.getCache("feed");
        followers.forEach(cache::evict);
    }
}

