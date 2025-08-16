package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.Service;

@Repository
public class ServiceRepository extends BaseRepository<Service> {

    public ServiceRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Service.class, "service");
    }

    public int save(Service entity) {
        String sql = "INSERT INTO service (icon, title, description, price, features) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                entity.getIcon(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getFeatures()
        );
    }

    public int update(Service entity) {
        String sql = "UPDATE service SET icon = ?, title = ?, description = ?, price = ?, features = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getIcon(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getFeatures(),
                entity.getId()
        );
    }
}
