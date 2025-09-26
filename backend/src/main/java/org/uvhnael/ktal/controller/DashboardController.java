package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.model.Blog;
import org.uvhnael.ktal.model.Contact;
import org.uvhnael.ktal.model.Project;
import org.uvhnael.ktal.model.Service;
import org.uvhnael.ktal.service.BlogService;
import org.uvhnael.ktal.service.ContactService;
import org.uvhnael.ktal.service.ProjectService;
import org.uvhnael.ktal.service.ServiceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final BlogService blogService;
    private final ProjectService projectService;
    private final ServiceService serviceService;
    private final ContactService contactService;

    /**
     * Retrieves comprehensive dashboard overview with key metrics
     *
     * @return Dashboard overview statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview() {
        log.info("GET /api/v1/dashboard/overview - Request for dashboard overview");

        try {
            // Fetch data from all services
            List<Blog> blogs = blogService.findAll();
            List<Project> projects = projectService.findAll();
            List<Service> services = serviceService.findAll();
            List<Contact> contacts = contactService.findAll();

            log.debug("Dashboard overview - Data loaded: {} blogs, {} projects, {} services, {} contacts",
                    blogs.size(), projects.size(), services.size(), contacts.size());

            // Calculate comprehensive overview statistics
            Map<String, Object> overview = new HashMap<>();

            // Blog metrics
            overview.put("totalBlogs", blogs.size());
            overview.put("publishedBlogs", blogs.stream()
                    .filter(blog -> AppConstants.EntityStatus.PUBLISHED.equalsIgnoreCase(blog.getStatus()))
                    .count());
            overview.put("draftBlogs", blogs.stream()
                    .filter(blog -> AppConstants.EntityStatus.DRAFT.equalsIgnoreCase(blog.getStatus()))
                    .count());

            // Project metrics
            overview.put("totalProjects", projects.size());
            overview.put("activeProjects", projects.stream()
                    .filter(project -> AppConstants.EntityStatus.ACTIVE.equalsIgnoreCase(project.getStatus()))
                    .count());
            overview.put("completedProjects", projects.stream()
                    .filter(project -> AppConstants.EntityStatus.COMPLETED.equalsIgnoreCase(project.getStatus()))
                    .count());

            // Service metrics
            overview.put("totalServices", services.size());
            overview.put("servicesWithFeatures", services.stream()
                    .filter(service -> service.getFeatures() != null && !service.getFeatures().isEmpty())
                    .count());

            // Contact metrics
            overview.put("totalContacts", contacts.size());
            overview.put("pendingContacts", contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.PENDING.equalsIgnoreCase(contact.getStatus()))
                    .count());
            overview.put("handledContacts", contacts.stream()
                    .filter(contact -> AppConstants.EntityStatus.HANDLED.equalsIgnoreCase(contact.getStatus()))
                    .count());

            log.info("GET /api/v1/dashboard/overview - Success: Generated overview with {} data points", overview.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.DASHBOARD_OVERVIEW_RETRIEVED, overview));

        } catch (Exception e) {
            log.error("GET /api/v1/dashboard/overview - Error generating overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve dashboard overview: " + e.getMessage()));
        }
    }

    /**
     * Retrieves recent activities across all entities
     *
     * @return Recent activities from blogs, projects, and contacts
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivities() {
        log.info("GET /api/v1/dashboard/recent-activities - Request for recent activities");

        try {
            List<Blog> blogs = blogService.findAll();
            List<Project> projects = projectService.findAll();
            List<Contact> contacts = contactService.findAll();

            Map<String, Object> activities = new HashMap<>();

            // Recent blogs (configurable limit)
            List<Blog> recentBlogs = blogs.stream()
                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                    .limit(AppConstants.Defaults.DEFAULT_RECENT_LIMIT / 2) // 5 items
                    .toList();
            activities.put("recentBlogs", recentBlogs);

            // Recent projects (configurable limit)
            List<Project> recentProjects = projects.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(AppConstants.Defaults.DEFAULT_RECENT_LIMIT / 2) // 5 items
                    .toList();
            activities.put("recentProjects", recentProjects);

            // Recent contacts (default limit)
            List<Contact> recentContacts = contacts.stream()
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                    .limit(AppConstants.Defaults.DEFAULT_RECENT_LIMIT) // 10 items
                    .toList();
            activities.put("recentContacts", recentContacts);

            log.info("GET /api/v1/dashboard/recent-activities - Success: Retrieved {} recent blogs, {} recent projects, {} recent contacts",
                    recentBlogs.size(), recentProjects.size(), recentContacts.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.RECENT_ACTIVITIES_RETRIEVED, activities));

        } catch (Exception e) {
            log.error("GET /api/v1/dashboard/recent-activities - Error retrieving recent activities: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent activities: " + e.getMessage()));
        }
    }

    /**
     * Retrieves content statistics grouped by categories and areas
     *
     * @return Content statistics for blogs and projects
     */
    @GetMapping("/content-statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContentStatistics() {
        log.info("GET /api/v1/dashboard/content-statistics - Request for content statistics");

        try {
            List<Blog> blogs = blogService.findAll();
            List<Project> projects = projectService.findAll();

            Map<String, Object> contentStats = new HashMap<>();

            // Blog categories distribution
            Map<String, Long> blogCategories = blogs.stream()
                    .filter(blog -> blog.getCategory() != null && !blog.getCategory().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Blog::getCategory,
                            java.util.stream.Collectors.counting()
                    ));
            contentStats.put("blogsByCategory", blogCategories);

            // Projects by area distribution
            Map<String, Long> projectAreas = projects.stream()
                    .filter(project -> project.getArea() != null && !project.getArea().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Project::getArea,
                            java.util.stream.Collectors.counting()
                    ));
            contentStats.put("projectsByArea", projectAreas);

            // Projects by year distribution
            Map<Integer, Long> projectYears = projects.stream()
                    .filter(project -> project.getYear() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            Project::getYear,
                            java.util.stream.Collectors.counting()
                    ));
            contentStats.put("projectsByYear", projectYears);

            log.info("GET /api/v1/dashboard/content-statistics - Success: Generated stats for {} blog categories, {} project areas, {} project years",
                    blogCategories.size(), projectAreas.size(), projectYears.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTENT_STATS_RETRIEVED, contentStats));

        } catch (Exception e) {
            log.error("GET /api/v1/dashboard/content-statistics - Error retrieving content statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve content statistics: " + e.getMessage()));
        }
    }

    /**
     * Retrieves contact analysis grouped by status, service, and handler
     *
     * @return Contact analysis data
     */
    @GetMapping("/contact-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContactAnalysis() {
        log.info("GET /api/v1/dashboard/contact-analysis - Request for contact analysis");

        try {
            List<Contact> contacts = contactService.findAll();
            List<Service> services = serviceService.findAll();

            Map<String, Object> analysis = new HashMap<>();

            // Contacts distribution by status
            Map<String, Long> contactsByStatus = contacts.stream()
                    .filter(contact -> contact.getStatus() != null && !contact.getStatus().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Contact::getStatus,
                            java.util.stream.Collectors.counting()
                    ));
            analysis.put("contactsByStatus", contactsByStatus);

            // Contacts distribution by service (with service name mapping)
            Map<Long, Long> contactsByServiceId = contacts.stream()
                    .filter(contact -> contact.getServiceId() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            Contact::getServiceId,
                            java.util.stream.Collectors.counting()
                    ));

            // Map service IDs to service names for better readability
            Map<String, Long> contactsByServiceName = new HashMap<>();
            for (Map.Entry<Long, Long> entry : contactsByServiceId.entrySet()) {
                Service service = services.stream()
                        .filter(s -> s.getId().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);
                String serviceName = service != null ? service.getTitle() : "Unknown Service";
                contactsByServiceName.put(serviceName, entry.getValue());
            }
            analysis.put("contactsByService", contactsByServiceName);

            // Contacts distribution by handler
            Map<String, Long> contactsByHandler = contacts.stream()
                    .filter(contact -> contact.getHandledBy() != null && !contact.getHandledBy().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Contact::getHandledBy,
                            java.util.stream.Collectors.counting()
                    ));
            analysis.put("contactsByHandler", contactsByHandler);

            log.info("GET /api/v1/dashboard/contact-analysis - Success: Analyzed {} status types, {} services, {} handlers",
                    contactsByStatus.size(), contactsByServiceName.size(), contactsByHandler.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CONTACT_ANALYSIS_RETRIEVED, analysis));

        } catch (Exception e) {
            log.error("GET /api/v1/dashboard/contact-analysis - Error retrieving contact analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve contact analysis: " + e.getMessage()));
        }
    }

    /**
     * Performs global search across all entities
     *
     * @param query Search query string
     * @return Search results from all entities
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> performGlobalSearch(@RequestParam String query) {
        log.info("GET /api/v1/dashboard/search - Global search request with query length: {} chars",
                query != null ? query.length() : 0);

        try {
            if (query == null || query.trim().isEmpty()) {
                log.warn("GET /api/v1/dashboard/search - Empty search query provided");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search query cannot be empty"));
            }

            String searchTerm = query.toLowerCase().trim();
            Map<String, Object> searchResults = new HashMap<>();

            // Search across all entity types with configurable limits
            searchResults.put("blogs", searchInBlogs(searchTerm));
            searchResults.put("projects", searchInProjects(searchTerm));
            searchResults.put("services", searchInServices(searchTerm));
            searchResults.put("contacts", searchInContacts(searchTerm));

            int totalResults = ((List<?>) searchResults.get("blogs")).size() +
                    ((List<?>) searchResults.get("projects")).size() +
                    ((List<?>) searchResults.get("services")).size() +
                    ((List<?>) searchResults.get("contacts")).size();

            searchResults.put("totalResults", totalResults);

            log.info("GET /api/v1/dashboard/search - Success: Found {} total results", totalResults);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.GLOBAL_SEARCH_COMPLETED, searchResults));

        } catch (Exception e) {
            log.error("GET /api/v1/dashboard/search - Error performing global search: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to perform global search: " + e.getMessage()));
        }
    }

    // Helper methods for global search functionality
    private List<Blog> searchInBlogs(String searchTerm) {
        return blogService.findAll().stream()
                .filter(blog -> blog.getTitle().toLowerCase().contains(searchTerm) ||
                        blog.getContent().toLowerCase().contains(searchTerm) ||
                        (blog.getCategory() != null && blog.getCategory().toLowerCase().contains(searchTerm)))
                .limit(AppConstants.Defaults.DEFAULT_SEARCH_LIMIT)
                .toList();
    }

    private List<Project> searchInProjects(String searchTerm) {
        return projectService.findAll().stream()
                .filter(project -> project.getTitle().toLowerCase().contains(searchTerm) ||
                        project.getDescription().toLowerCase().contains(searchTerm) ||
                        (project.getArea() != null && project.getArea().toLowerCase().contains(searchTerm)))
                .limit(AppConstants.Defaults.DEFAULT_SEARCH_LIMIT)
                .toList();
    }

    private List<Service> searchInServices(String searchTerm) {
        return serviceService.findAll().stream()
                .filter(service -> service.getTitle().toLowerCase().contains(searchTerm) ||
                        service.getDescription().toLowerCase().contains(searchTerm) ||
                        (service.getFeatures() != null && service.getFeatures().toLowerCase().contains(searchTerm)))
                .limit(AppConstants.Defaults.DEFAULT_SEARCH_LIMIT)
                .toList();
    }

    private List<Contact> searchInContacts(String searchTerm) {
        return contactService.findAll().stream()
                .filter(contact -> contact.getName().toLowerCase().contains(searchTerm) ||
                        contact.getEmail().toLowerCase().contains(searchTerm) ||
                        contact.getMessage().toLowerCase().contains(searchTerm))
                .limit(AppConstants.Defaults.DEFAULT_SEARCH_LIMIT)
                .toList();
    }
}
