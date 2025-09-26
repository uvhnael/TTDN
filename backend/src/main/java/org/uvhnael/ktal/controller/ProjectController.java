package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.model.Project;
import org.uvhnael.ktal.service.ProjectService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Retrieves all projects with optional filtering
     *
     * @param year   Filter by project year
     * @param area   Filter by project area
     * @param status Filter by project status
     * @param search Search in title and description
     * @return List of projects matching the criteria
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        log.info("GET /api/v1/projects - Request params: year={}, area={}, status={}, search={}",
                year, area, status, search);

        try {
            List<Project> projects = projectService.findAll();
            int originalSize = projects.size();

            // Apply year filter if provided and within valid range
            if (year != null) {
                if (year < AppConstants.Validation.MIN_PROJECT_YEAR || year > AppConstants.Validation.MAX_PROJECT_YEAR) {
                    log.warn("Invalid year filter: {}", year);
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Year must be between " + AppConstants.Validation.MIN_PROJECT_YEAR +
                                    " and " + AppConstants.Validation.MAX_PROJECT_YEAR));
                }
                projects = projects.stream()
                        .filter(project -> year.equals(project.getYear()))
                        .toList();
                log.debug("{} year '{}': {} -> {} projects", AppConstants.LogMessages.DATA_FILTERED,
                        year, originalSize, projects.size());
            }

            // Apply area filter if provided
            if (area != null && !area.isEmpty()) {
                projects = projects.stream()
                        .filter(project -> area.equalsIgnoreCase(project.getArea()))
                        .toList();
                log.debug("{} area '{}': {} projects remaining", AppConstants.LogMessages.DATA_FILTERED,
                        area, projects.size());
            }

            // Apply status filter if provided
            if (status != null && !status.isEmpty()) {
                projects = projects.stream()
                        .filter(project -> status.equalsIgnoreCase(project.getStatus()))
                        .toList();
                log.debug("{} status '{}': {} projects remaining", AppConstants.LogMessages.DATA_FILTERED,
                        status, projects.size());
            }

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                projects = projects.stream()
                        .filter(project -> project.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                project.getDescription().toLowerCase().contains(search.toLowerCase()))
                        .toList();
                log.debug("{} search: {} projects remaining", AppConstants.LogMessages.DATA_FILTERED, projects.size());
            }

            log.info("GET /api/v1/projects - Success: Retrieved {} projects (filtered from {} total)",
                    projects.size(), originalSize);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECTS_RETRIEVED, projects));

        } catch (Exception e) {
            log.error("GET /api/v1/projects - Error retrieving projects: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve projects: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific project by ID
     *
     * @param id Project ID
     * @return Project details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        log.info("GET /api/v1/projects/{} - Request to get project by ID", id);

        try {
            Project project = projectService.findById(id).orElse(null);
            if (project != null) {
                log.info("GET /api/v1/projects/{} - {}: Found project with title '{}'",
                        id, AppConstants.LogMessages.ENTITY_FOUND, project.getTitle());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_RETRIEVED, project));
            } else {
                log.warn("GET /api/v1/projects/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/projects/{} - Error retrieving project: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve project: " + e.getMessage()));
        }
    }

    /**
     * Retrieves project by slug
     *
     * @param slug Project slug
     * @return Project details
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Project>> getProjectBySlug(@PathVariable String slug) {
        log.info("GET /api/v1/projects/slug/{} - Request to get project by slug", slug);

        try {
            List<Project> projects = projectService.findAll();
            Project project = projects.stream()
                    .filter(p -> slug.equals(p.getSlug()))
                    .findFirst()
                    .orElse(null);

            if (project != null) {
                log.info("GET /api/v1/projects/slug/{} - {}: Found project with ID {}",
                        slug, AppConstants.LogMessages.ENTITY_FOUND, project.getId());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_RETRIEVED, project));
            } else {
                log.warn("GET /api/v1/projects/slug/{} - {}", slug, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/projects/slug/{} - Error retrieving project: {}", slug, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve project: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all unique project areas
     *
     * @return List of distinct project areas
     */
    @GetMapping("/areas")
    public ResponseEntity<ApiResponse<List<String>>> getProjectAreas() {
        log.info("GET /api/v1/projects/areas - Request to get project areas");

        try {
            List<Project> projects = projectService.findAll();
            List<String> areas = projects.stream()
                    .map(Project::getArea)
                    .distinct()
                    .filter(area -> area != null && !area.isEmpty())
                    .toList();

            log.info("GET /api/v1/projects/areas - Success: Retrieved {} unique areas", areas.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_AREAS_RETRIEVED, areas));

        } catch (Exception e) {
            log.error("GET /api/v1/projects/areas - Error retrieving project areas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve project areas: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all project years in descending order
     *
     * @return List of project years
     */
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getProjectYears() {
        log.info("GET /api/v1/projects/years - Request to get project years");

        try {
            List<Project> projects = projectService.findAll();
            List<Integer> years = projects.stream()
                    .map(Project::getYear)
                    .distinct()
                    .filter(year -> year != null)
                    .sorted((a, b) -> b.compareTo(a)) // Sort descending
                    .toList();

            log.info("GET /api/v1/projects/years - Success: Retrieved {} unique years", years.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_YEARS_RETRIEVED, years));

        } catch (Exception e) {
            log.error("GET /api/v1/projects/years - Error retrieving project years: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve project years: " + e.getMessage()));
        }
    }

    /**
     * Retrieves project statistics
     *
     * @return Project statistics data
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectStatistics() {
        log.info("GET /api/v1/projects/statistics - Request to get project statistics");

        try {
            List<Project> projects = projectService.findAll();

            long totalProjects = projects.size();
            long activeProjects = projects.stream()
                    .filter(project -> AppConstants.EntityStatus.ACTIVE.equalsIgnoreCase(project.getStatus()))
                    .count();
            long completedProjects = projects.stream()
                    .filter(project -> AppConstants.EntityStatus.COMPLETED.equalsIgnoreCase(project.getStatus()))
                    .count();

            Map<String, Object> statistics = Map.of(
                    "totalProjects", totalProjects,
                    "activeProjects", activeProjects,
                    "completedProjects", completedProjects,
                    "uniqueAreas", projects.stream().map(Project::getArea).distinct().count(),
                    "uniqueYears", projects.stream().map(Project::getYear).distinct().count()
            );

            log.info("GET /api/v1/projects/statistics - Success: Stats calculated - Total: {}, Active: {}, Completed: {}",
                    totalProjects, activeProjects, completedProjects);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.STATISTICS_RETRIEVED, statistics));

        } catch (Exception e) {
            log.error("GET /api/v1/projects/statistics - Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve project statistics: " + e.getMessage()));
        }
    }

    /**
     * Retrieves recent projects with configurable limit
     *
     * @param limit Maximum number of projects to return
     * @return List of recent projects
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Project>>> getRecentProjects(
            @RequestParam(defaultValue = "5") int limit) {

        // Validate limit parameter
        if (limit > AppConstants.Defaults.MAX_PAGE_SIZE) {
            limit = AppConstants.Defaults.MAX_PAGE_SIZE;
        }

        log.info("GET /api/v1/projects/recent - Request to get {} recent projects", limit);

        try {
            List<Project> projects = projectService.findAll();
            List<Project> recentProjects = projects.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(limit)
                    .toList();

            log.info("GET /api/v1/projects/recent - Success: Retrieved {} recent projects", recentProjects.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECTS_RETRIEVED, recentProjects));

        } catch (Exception e) {
            log.error("GET /api/v1/projects/recent - Error retrieving recent projects: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent projects: " + e.getMessage()));
        }
    }

    /**
     * Creates a new project
     *
     * @param project Project data to create
     * @return Success message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createProject(@RequestBody Project project) {
        log.info("POST /api/v1/projects - Request to create project with title '{}'", project.getTitle());

        try {
            int result = projectService.create(project);
            log.info("POST /api/v1/projects - {}: Created project with title '{}'",
                    AppConstants.LogMessages.ENTITY_CREATED, project.getTitle());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_CREATED,
                    "Created " + result + " record(s)"));

        } catch (Exception e) {
            log.error("POST /api/v1/projects - Error creating project: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create project: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing project
     *
     * @param id      Project ID
     * @param project Updated project data
     * @return Update result
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateProject(@PathVariable Long id, @RequestBody Project project) {
        log.info("PUT /api/v1/projects/{} - Request to update project", id);

        try {
            project.setId(id);
            int result = projectService.update(project);
            if (result > 0) {
                log.info("PUT /api/v1/projects/{} - {}: Updated {} record(s)",
                        id, AppConstants.LogMessages.ENTITY_UPDATED, result);
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_UPDATED,
                        "Updated " + result + " record(s)"));
            } else {
                log.warn("PUT /api/v1/projects/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/projects/{} - Error updating project: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update project: " + e.getMessage()));
        }
    }

    /**
     * Deletes a project
     *
     * @param id Project ID
     * @return Deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        log.info("DELETE /api/v1/projects/{} - Request to delete project", id);

        try {
            int result = projectService.delete(id);
            if (result > 0) {
                log.info("DELETE /api/v1/projects/{} - {}: Deleted {} record(s)",
                        id, AppConstants.LogMessages.ENTITY_DELETED, result);
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.PROJECT_DELETED,
                        "Deleted " + result + " record(s)"));
            } else {
                log.warn("DELETE /api/v1/projects/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/v1/projects/{} - Error deleting project: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete project: " + e.getMessage()));
        }
    }
}
