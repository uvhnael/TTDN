package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.model.Contact;
import org.uvhnael.ktal.service.ContactService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Contact> getContactById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @PostMapping
    public int createContact(@RequestBody Contact contact) {
        return contactService.create(contact);
    }

    @PutMapping("/{id}")
    public int updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(id);
        return contactService.update(contact);
    }

    @DeleteMapping("/{id}")
    public int deleteContact(@PathVariable Long id) {
        return contactService.delete(id);
    }

    @PutMapping("/{id}/note")
    public int updateContactNote(@PathVariable Long id, @RequestParam String note) {
        return contactService.updateNote(id, note);
    }

    @PutMapping("/{id}/status")
    public int updateContactStatus(@PathVariable Long id, @RequestParam String status) {
        return contactService.updateStatus(id, status);
    }

    @PutMapping("/{id}/handled")
    public int updateContactHandled(@PathVariable Long id,
                                    @RequestParam Long handleBy,
                                    @RequestParam String handledAt) {
        return contactService.updateHandled(handleBy, handledAt, id);
    }
}
