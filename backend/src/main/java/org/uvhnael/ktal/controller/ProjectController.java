package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.model.Project;
import org.uvhnael.ktal.model.ProjectImage;
import org.uvhnael.ktal.service.ProjectService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.findById(id);
        return project.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Integer> createProject(@RequestBody Project project) {
        int result = projectService.create(project);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Integer> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        int result = projectService.update(project);
        return result > 0
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> deleteProject(@PathVariable Long id) {
        int result = projectService.delete(id);
        return result > 0
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/images")
    public ResponseEntity<Integer> addProjectImage(@RequestBody ProjectImage image) {
        int result = projectService.createImage(image);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Integer> deleteProjectImage(@PathVariable Long id) {
        int result = projectService.deleteImage(id);
        return result > 0
                ? ResponseEntity.ok(result)
                : ResponseEntity.notFound().build();
    }
}
