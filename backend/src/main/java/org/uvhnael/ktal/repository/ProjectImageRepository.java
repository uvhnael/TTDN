package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.ProjectImage;

import java.util.List;

@Repository
public class ProjectImageRepository extends BaseRepository<ProjectImage> {

    public ProjectImageRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, ProjectImage.class, "project_image");
    }

    public int save(ProjectImage entity) {
        String sql = "INSERT INTO project_image (project_id, image_url) VALUES (?, ?)";
        return jdbcTemplate.update(sql,
                entity.getProjectId(),
                entity.getImageUrl()
        );
    }

    public int update(ProjectImage entity) {
        String sql = "UPDATE project_image SET project_id = ?, image_url = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getProjectId(),
                entity.getImageUrl(),
                entity.getId()
        );
    }

    public List<ProjectImage> findByProjectId(Long projectId) {
        String sql = "SELECT * FROM project_image WHERE project_id = ?";
        return jdbcTemplate.query(sql, new Object[]{projectId}, (rs, rowNum) -> {
            ProjectImage image = new ProjectImage();
            image.setId(rs.getLong("id"));
            image.setProjectId(rs.getLong("project_id"));
            image.setImageUrl(rs.getString("image_url"));
            return image;
        });
    }
}
