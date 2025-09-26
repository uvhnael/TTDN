package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.model.Contact;
import org.uvhnael.ktal.service.ContactService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    /**
     * Retrieves all contacts with optional filtering
     *
     * @param status    Filter by contact status
     * @param serviceId Filter by service ID
     * @param handledBy Filter by handler
     * @param search    Search in name, email, and message
     * @return List of contacts matching the criteria
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Contact>>> getAllContacts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String handledBy,
            @RequestParam(required = false) String search) {

        log.info("GET /api/v1/contacts - Request params: status={}, serviceId={}, handledBy={}, search={}",
                status, serviceId, handledBy, search);

        try {
            List<Contact> contacts = contactService.findAll();
            int originalSize = contacts.size();

            // Apply status filter if provided
            if (status != null && !status.isEmpty()) {
                contacts = contacts.stream()
                        .filter(contact -> status.equalsIgnoreCase(contact.getStatus()))
                        .toList();
                log.debug("{} '{}': {} -> {} contacts", AppConstants.LogMessages.DATA_FILTERED,
                        status, originalSize, contacts.size());
            }

            // Apply service ID filter if provided
            if (serviceId != null) {
                contacts = contacts.stream()
                        .filter(contact -> serviceId.equals(contact.getServiceId()))
                        .toList();
                log.debug("{} serviceId '{}': {} contacts remaining", AppConstants.LogMessages.DATA_FILTERED,
                        serviceId, contacts.size());
            }

            // Apply handler filter if provided
            if (handledBy != null && !handledBy.isEmpty()) {
                contacts = contacts.stream()
                        .filter(contact -> handledBy.equalsIgnoreCase(contact.getHandledBy()))
                        .toList();
                log.debug("{} handledBy '{}': {} contacts remaining", AppConstants.LogMessages.DATA_FILTERED,
                        handledBy, contacts.size());
            }

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                contacts = contacts.stream()
                        .filter(contact -> contact.getName().toLowerCase().contains(search.toLowerCase()) ||
                                contact.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                                contact.getMessage().toLowerCase().contains(search.toLowerCase()))
                        .toList();
                log.debug("{} search: {} contacts remaining", AppConstants.LogMessages.DATA_FILTERED, contacts.size());
            }

            log.info("GET /api/v1/contacts - Success: Retrieved {} contacts (filtered from {} total)",
                    contacts.size(), originalSize);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACTS_RETRIEVED, contacts));

        } catch (Exception e) {
            log.error("GET /api/v1/contacts - Error retrieving contacts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contacts: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific contact by ID
     *
     * @param id Contact ID
     * @return Contact details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Contact>> getContactById(@PathVariable Long id) {
        log.info("GET /api/v1/contacts/{} - Request to get contact by ID", id);

        try {
            Contact contact = contactService.findById(id).orElse(null);
            if (contact != null) {
                log.info("GET /api/v1/contacts/{} - {}: Found contact from '{}' with email '{}'",
                        id, AppConstants.LogMessages.ENTITY_FOUND, contact.getName(), contact.getEmail());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACT_RETRIEVED, contact));
            } else {
                log.warn("GET /api/v1/contacts/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/contacts/{} - Error retrieving contact: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all pending contacts
     *
     * @return List of pending contacts
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Contact>>> getPendingContacts() {
        log.info("GET /api/v1/contacts/pending - Request to get pending contacts");

        try {
            List<Contact> contacts = contactService.findAll();
            List<Contact> pendingContacts = contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.PENDING.equalsIgnoreCase(contact.getStatus()))
                    .toList();

            log.info("GET /api/v1/contacts/pending - Success: Retrieved {} pending contacts from {} total",
                    pendingContacts.size(), contacts.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACTS_RETRIEVED, pendingContacts));

        } catch (Exception e) {
            log.error("GET /api/v1/contacts/pending - Error retrieving pending contacts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending contacts: " + e.getMessage()));
        }
    }

    /**
     * Retrieves recent contacts with configurable limit
     *
     * @param limit Maximum number of contacts to return (default: 10)
     * @return List of recent contacts
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Contact>>> getRecentContacts(
            @RequestParam(defaultValue = "10") int limit) {

        // Validate limit parameter to prevent excessive data retrieval
        if (limit > AppConstants.Defaults.MAX_PAGE_SIZE) {
            limit = AppConstants.Defaults.MAX_PAGE_SIZE;
        }

        log.info("GET /api/v1/contacts/recent - Request to get {} recent contacts", limit);

        try {
            List<Contact> contacts = contactService.findAll();
            List<Contact> recentContacts = contacts.stream()
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                    .limit(limit)
                    .toList();

            log.info("GET /api/v1/contacts/recent - Success: Retrieved {} recent contacts", recentContacts.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACTS_RETRIEVED, recentContacts));

        } catch (Exception e) {
            log.error("GET /api/v1/contacts/recent - Error retrieving recent contacts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent contacts: " + e.getMessage()));
        }
    }

    /**
     * Retrieves contact statistics grouped by status and service
     *
     * @return Contact statistics data
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContactStatistics() {
        log.info("GET /api/v1/contacts/statistics - Request to get contact statistics");

        try {
            List<Contact> contacts = contactService.findAll();

            long totalContacts = contacts.size();
            long pendingContacts = contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.PENDING.equalsIgnoreCase(contact.getStatus()))
                    .count();
            long handledContacts = contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.HANDLED.equalsIgnoreCase(contact.getStatus()))
                    .count();
            long closedContacts = contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.CLOSED.equalsIgnoreCase(contact.getStatus()))
                    .count();

            Map<String, Object> statistics = Map.of(
                    "totalContacts", totalContacts,
                    "pendingContacts", pendingContacts,
                    "handledContacts", handledContacts,
                    "closedContacts", closedContacts,
                    "uniqueServices", contacts.stream().map(Contact::getServiceId).distinct().count()
            );

            log.info("GET /api/v1/contacts/statistics - Success: Stats calculated - Total: {}, Pending: {}, Handled: {}, Closed: {}",
                    totalContacts, pendingContacts, handledContacts, closedContacts);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.STATISTICS_RETRIEVED, statistics));

        } catch (Exception e) {
            log.error("GET /api/v1/contacts/statistics - Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact statistics: " + e.getMessage()));
        }
    }

    /**
     * Creates a new contact record
     *
     * @param contact Contact information to be created
     * @return ResponseEntity with created contact data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Contact>> createContact(@RequestBody Contact contact) {
        log.info("POST /api/v1/contacts - Request to create contact from '{}' with email '{}' for serviceId '{}'",
                contact.getName(), contact.getEmail(), contact.getServiceId());

        try {
            Contact createdContact = contactService.create(contact);
            log.info("POST /api/v1/contacts - {}: Created contact with ID {} from '{}'",
                    AppConstants.LogMessages.ENTITY_CREATED, createdContact.getId(), createdContact.getName());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACT_CREATED, createdContact));

        } catch (Exception e) {
            log.error("POST /api/v1/contacts - Error creating contact from '{}': {}",
                    contact.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create contact: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing contact
     *
     * @param id      Contact ID to update
     * @param contact Updated contact information
     * @return ResponseEntity with update result
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        log.info("PUT /api/v1/contacts/{} - Request to update contact from '{}'",
                id, contact.getName());

        try {
            contact.setId(id);
            int affectedRows = contactService.update(contact);
            if (affectedRows > 0) {
                log.info("PUT /api/v1/contacts/{} - Success: Updated {} record(s)", id, affectedRows);
                return ResponseEntity.ok(ApiResponse.success("Contact updated successfully",
                        String.format("Updated %d record(s)", affectedRows)));
            } else {
                log.warn("PUT /api/v1/contacts/{} - Contact not found for update", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/contacts/{} - Error updating contact: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update contact: " + e.getMessage()));
        }
    }

    /**
     * Updates contact note
     *
     * @param id   Contact ID
     * @param note New note content
     * @return ResponseEntity with update result
     */
    @PutMapping("/{id}/note")
    public ResponseEntity<ApiResponse<String>> updateContactNote(@PathVariable Long id, @RequestParam String note) {
        log.info("PUT /api/v1/contacts/{}/note - Request to update contact note (note length: {} chars)",
                id, note != null ? note.length() : 0);

        try {
            int result = contactService.updateNote(id, note);
            if (result > 0) {
                log.info("PUT /api/v1/contacts/{}/note - Success: Updated note for contact", id);
                return ResponseEntity.ok(ApiResponse.success("Contact note updated successfully",
                        "Updated note for contact ID: " + id));
            } else {
                log.warn("PUT /api/v1/contacts/{}/note - Contact not found for note update", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/contacts/{}/note - Error updating contact note: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update contact note: " + e.getMessage()));
        }
    }

    /**
     * Updates contact status
     *
     * @param id     Contact ID
     * @param status New status
     * @return ResponseEntity with update result
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateContactStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("PUT /api/v1/contacts/{}/status - Request to update contact status to '{}'", id, status);

        try {
            int result = contactService.updateStatus(id, status);
            if (result > 0) {
                log.info("PUT /api/v1/contacts/{}/status - Success: Updated status to '{}'", id, status);
                return ResponseEntity.ok(ApiResponse.success("Contact status updated successfully",
                        "Updated status to: " + status));
            } else {
                log.warn("PUT /api/v1/contacts/{}/status - Contact not found for status update", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/contacts/{}/status - Error updating contact status: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update contact status: " + e.getMessage()));
        }
    }

    /**
     * Deletes a contact by ID
     *
     * @param id Contact ID to delete
     * @return ResponseEntity with deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteContact(@PathVariable Long id) {
        log.info("DELETE /api/v1/contacts/{} - Request to delete contact", id);

        try {
            int result = contactService.delete(id);
            if (result > 0) {
                log.info("DELETE /api/v1/contacts/{} - Success: Deleted {} record(s)", id, result);
                return ResponseEntity.ok(ApiResponse.success("Contact deleted successfully",
                        "Deleted " + result + " record(s)"));
            } else {
                log.warn("DELETE /api/v1/contacts/{} - Contact not found for deletion", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/v1/contacts/{} - Error deleting contact: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete contact: " + e.getMessage()));
        }
    }
}
