package org.uvhnael.ktal.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.uvhnael.ktal.model.Contact;

@Repository
public class ContactRepository extends BaseRepository<Contact> {

    public ContactRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Contact.class, "contact");
    }

    public int save(Contact entity) {
        String sql = "INSERT INTO contact (name, phone, email, service_id, message, status, created_at) VALUES (?, ?, ?, ?,?, ?, ?)";
        return jdbcTemplate.update(sql,
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getServiceId(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    public int update(Contact entity) {
        String sql = "UPDATE contact SET name = ?, phone = ?, email = ?, service_id = ?, message = ?, created_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getServiceId(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getId()
        );
    }

    public int updateNote(Long id, String note) {
        String sql = "UPDATE contact SET note = ? WHERE id = ?";
        return jdbcTemplate.update(sql, note, id);
    }

    public int updateStatus(Long id, String status) {
        String sql = "UPDATE contact SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public int updateHandled(Long handledBy, String handledAt, Long id) {
        String sql = "UPDATE contact SET handled_by = ?, handled_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, handledBy, handledAt, id);
    }

}
