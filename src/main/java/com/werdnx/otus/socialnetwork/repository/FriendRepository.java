package com.werdnx.otus.socialnetwork.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepository {
    private final JdbcTemplate jdbc;

    public void add(Long userId, Long friendId) {
        jdbc.update(
                "INSERT INTO friend(user_id, friend_id) VALUES (?,?)",
                userId, friendId);
    }

    public void delete(Long userId, Long friendId) {
        jdbc.update(
                "DELETE FROM friend WHERE user_id=? AND friend_id=?",
                userId, friendId);
    }

    public List<Long> findFriends(Long userId) {
        return jdbc.query(
                "SELECT friend_id FROM friend WHERE user_id=?",
                (rs, i) -> rs.getLong("friend_id"), userId);
    }

    public List<Long> findFollowers(Long userId) {
        return jdbc.query(
                "SELECT user_id FROM friend WHERE friend_id=?",
                (rs, i) -> rs.getLong("user_id"), userId);
    }
}