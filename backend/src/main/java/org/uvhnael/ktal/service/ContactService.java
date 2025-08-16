package org.uvhnael.ktal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uvhnael.ktal.model.Contact;
import org.uvhnael.ktal.repository.ContactRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public Optional<Contact> findById(Long id) {
        return contactRepository.findById(id);
    }

    public int create(Contact contact) {
        contact.setStatus("Đang chờ xử lý");
        return contactRepository.save(contact);
    }

    public int update(Contact contact) {
        return contactRepository.update(contact);
    }

    public int delete(Long id) {
        return contactRepository.deleteById(id);
    }

    public int updateNote(Long id, String note) {
        return contactRepository.updateNote(id, note);
    }

    public int updateStatus(Long id, String status) {
        return contactRepository.updateStatus(id, status);
    }

    public int updateHandled(Long handleBy, String handledAt, Long id ) {
        return contactRepository.updateHandled(handleBy, handledAt, id);
    }
}
