package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.Blog;

@Repository
public class BlogRepository extends BaseRepository<Blog> {

    public BlogRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Blog.class, "blog");
    }

    public Blog save(Blog entity) {
        String sql = "INSERT INTO blog (title, slug, author, category, thumbnail, content, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getSlug(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getThumbnail(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        entity.setId(id);
        return entity;

    }

    public int update(Blog entity) {
        String sql = "UPDATE blog SET title = ?, slug = ?, author = ?, category = ?, thumbnail = ?, content = ?, status = ?, created_at = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getSlug(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getThumbnail(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getId()
        );
    }

    public Blog findBySlug(String slug) {
        String sql = "SELECT * FROM blog WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{slug}, (rs, rowNum) -> {
            Blog blog = new Blog();
            blog.setId(rs.getLong("id"));
            blog.setTitle(rs.getString("title"));
            blog.setSlug(rs.getString("slug"));
            blog.setAuthor(rs.getString("author"));
            blog.setCategory(rs.getString("category"));
            blog.setThumbnail(rs.getString("thumbnail"));
            blog.setContent(rs.getString("content"));
            blog.setStatus(rs.getString("status"));
            blog.setCreatedAt(rs.getString("created_at"));
            blog.setUpdatedAt(rs.getString("updated_at"));
            return blog;
        });
    }


}
