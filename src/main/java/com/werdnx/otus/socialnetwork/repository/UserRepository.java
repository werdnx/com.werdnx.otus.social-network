package com.werdnx.otus.socialnetwork.repository;

import com.werdnx.otus.socialnetwork.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int countById(Long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM app_user WHERE id = ?", Integer.class, id);
    }

    public User findById(Long id) {
        return jdbc.queryForObject(
            "SELECT id, first_name, last_name, birth_date, gender, interests, city, password_hash FROM app_user WHERE id = ?",
            new UserMapper(), id);
    }

    public void save(User user) {
        jdbc.update(
            "INSERT INTO app_user (first_name, last_name, birth_date, gender, interests, city, password_hash) VALUES (?, ?, ?, ?, ?, ?, ?)",
            user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getGender(),
            user.getInterests(), user.getCity(), user.getPasswordHash()
        );
    }

    static class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setFirstName(rs.getString("first_name"));
            u.setLastName(rs.getString("last_name"));
            u.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
            u.setGender(rs.getString("gender"));
            u.setInterests(rs.getString("interests"));
            u.setCity(rs.getString("city"));
            u.setPasswordHash(rs.getString("password_hash"));
            return u;
        }
    }
}
