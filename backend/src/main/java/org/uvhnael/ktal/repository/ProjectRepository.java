package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.Project;


@Repository
public class ProjectRepository extends BaseRepository<Project> {

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Project.class, "project");
    }

    public int save(Project entity) {
        String sql = "INSERT INTO project (title, content, year, area, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getContent(),
                entity.getYear(),
                entity.getArea(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public int update(Project entity) {
        String sql = "UPDATE project SET title = ?, content = ?, year = ?, area = ?, status = ?, created_at = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getTitle(),
                entity.getContent(),
                entity.getYear(),
                entity.getArea(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getId()
        );
    }
}
