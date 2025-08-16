package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.model.Service;
import org.uvhnael.ktal.service.ServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
        return serviceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createService(@RequestBody Service service) {
        int rows = serviceService.create(service);
        if (rows > 0) {
            return ResponseEntity.ok("Service created successfully");
        }
        return ResponseEntity.badRequest().body("Failed to create service");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateService(@PathVariable Long id, @RequestBody Service service) {
        service.setId(id);
        int rows = serviceService.update(service);
        if (rows > 0) {
            return ResponseEntity.ok("Service updated successfully");
        }
        return ResponseEntity.badRequest().body("Failed to update service");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        int rows = serviceService.delete(id);
        if (rows > 0) {
            return ResponseEntity.ok("Service deleted successfully");
        }
        return ResponseEntity.badRequest().body("Failed to delete service");
    }
}
