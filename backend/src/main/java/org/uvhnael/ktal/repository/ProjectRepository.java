package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.Project;


@Repository
public class ProjectRepository extends BaseRepository<Project> {

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Project.class, "project");
    }

//    private Long id;
//    private String title;
//    private String slug;
//    private String description;
//    private Integer year;
//    private String area;
//    private String thumbnail;
//    private String content;   // HTML hoặc text
//    private String status;
//    private String createdAt;
//    private String updatedAt;

    public int save(Project entity) {
        String sql = "INSERT INTO project (title, slug, description, year, area, thumbnail, content, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getYear(),
                entity.getArea(),
                entity.getThumbnail(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public int update(Project entity) {
        String sql = "UPDATE project SET title = ?, slug = ?, description = ?, year = ?, area = ?, thumbnail = ?, content = ?, status = ?, created_at = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getYear(),
                entity.getArea(),
                entity.getThumbnail(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getId()
        );
    }

    public Project findBySlug(String slug) {
        String sql = "SELECT * FROM project WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{slug}, (rs, rowNum) -> {
            Project project = new Project();
            project.setId(rs.getLong("id"));
            project.setTitle(rs.getString("title"));
            project.setSlug(rs.getString("slug"));
            project.setDescription(rs.getString("description"));
            project.setYear(rs.getInt("year"));
            project.setArea(rs.getString("area"));
            project.setThumbnail(rs.getString("thumbnail"));
            project.setContent(rs.getString("content"));
            project.setStatus(rs.getString("status"));
            project.setCreatedAt(rs.getString("created_at"));
            project.setUpdatedAt(rs.getString("updated_at"));
            return project;
        });
    }
}
