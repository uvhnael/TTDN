package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.User;

@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, User.class, "user");
    }

    public int save(User entity) {
        String sql = "INSERT INTO user (email, username, password, role) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                entity.getEmail(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole()
        );
    }

    public int update(User entity) {
        String sql = "UPDATE user SET email = ?, username = ?, password = ?, role = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getEmail(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getId()
        );
    }
}
