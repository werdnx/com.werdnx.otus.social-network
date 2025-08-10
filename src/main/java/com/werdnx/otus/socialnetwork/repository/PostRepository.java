package com.werdnx.otus.socialnetwork.repository;

import com.werdnx.otus.socialnetwork.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Post> rm = (rs, i) -> {
        Post p = new Post();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setContent(rs.getString("content"));
        p.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return p;
    };

    public Long create(Post p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO post(user_id,content,created_at) VALUES(?,?,?)",
                    new String[] {"id"});
            ps.setLong(1, p.getUserId());
            ps.setString(2, p.getContent());
            ps.setTimestamp(3, Timestamp.from(p.getCreatedAt()));
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void update(Post p) {
        jdbc.update(
                "UPDATE post SET content=?, created_at=? WHERE id=?",
                p.getContent(),
                Timestamp.from(p.getCreatedAt()),
                p.getId());
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM post WHERE id=?", id);
    }

    public Post findById(Long id) {
        return jdbc.queryForObject(
                "SELECT * FROM post WHERE id=?",
                rm, id);
    }

    public List<Post> findRecentByUsers(List<Long> users, int limit) {
        if (users.isEmpty()) return List.of();
        String in = users.stream().map(u->"?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM post WHERE user_id IN(" + in + ") "
                + "ORDER BY created_at DESC LIMIT ?";
        List<Object> args = new ArrayList<>(users);
        args.add(limit);
        return jdbc.query(sql, rm, args.toArray());
    }
    public Long insert(Long userId, String content, Instant createdAt){
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO post(user_id, content, created_at) VALUES (?,?,?)",
                    new String[] {"id"});
            ps.setLong(1, userId);
            ps.setString(2, content);
            ps.setTimestamp(3, Timestamp.from(createdAt));
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? null : key.longValue();
    }
}
