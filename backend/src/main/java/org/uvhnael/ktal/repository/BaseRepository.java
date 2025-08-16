package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> {

    protected final JdbcTemplate jdbcTemplate;
    private final Class<T> type;
    private final String tableName;

    public BaseRepository(JdbcTemplate jdbcTemplate, Class<T> type, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.type = type;
        this.tableName = tableName;
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(type));
    }

    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        List<T> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(type), id);
        return results.stream().findFirst();
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

//    public abstract Long save(T entity);
//
//    public abstract int update(T entity);
}
