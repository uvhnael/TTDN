package org.uvhnael.ktal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.model.Blog;
import org.uvhnael.ktal.service.BlogService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlogController {

    private final BlogService blogService;

    /**
     * Retrieves all blogs with optional filtering and pagination
     *
     * @param page     Page number (default: 0)
     * @param size     Page size (default: 10, max: 100)
     * @param category Filter by blog category
     * @param status   Filter by blog status (published, draft, archived)
     * @param search   Search in title and content
     * @return List of blogs matching the criteria
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        // Validate and limit page size to prevent excessive data retrieval
        if (size > AppConstants.Defaults.MAX_PAGE_SIZE) {
            size = AppConstants.Defaults.MAX_PAGE_SIZE;
        }

        log.info("GET /api/v1/blogs - Request params: page={}, size={}, category={}, status={}, search={}",
                page, size, category, status, search);

        try {
            List<Blog> blogs = blogService.findAll();
            int originalSize = blogs.size();

            // Apply category filter if provided
            if (category != null && !category.isEmpty()) {
                blogs = blogs.stream()
                        .filter(blog -> category.equals(blog.getCategory()))
                        .toList();
                log.debug("{} category '{}': {} -> {} blogs", AppConstants.LogMessages.DATA_FILTERED,
                        category, originalSize, blogs.size());
            }

            // Apply status filter if provided
            if (status != null && !status.isEmpty()) {
                blogs = blogs.stream()
                        .filter(blog -> status.equals(blog.getStatus()))
                        .toList();
                log.debug("{} status '{}': {} blogs remaining", AppConstants.LogMessages.DATA_FILTERED,
                        status, blogs.size());
            }

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                blogs = blogs.stream()
                        .filter(blog -> blog.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                blog.getContent().toLowerCase().contains(search.toLowerCase()))
                        .toList();
                log.debug("{} search: {} blogs remaining", AppConstants.LogMessages.DATA_FILTERED, blogs.size());
            }

            log.info("GET /api/v1/blogs - Success: Retrieved {} blogs (filtered from {} total)",
                    blogs.size(), originalSize);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.BLOGS_RETRIEVED, blogs));

        } catch (Exception e) {
            log.error("GET /api/v1/blogs - Error retrieving blogs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve blogs: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific blog by ID
     *
     * @param id Blog ID
     * @return Blog details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> getBlogById(@PathVariable Long id) {
        log.info("GET /api/v1/blogs/{} - Request to get blog by ID", id);

        try {
            Blog blog = blogService.findById(id).orElse(null);
            if (blog != null) {
                log.info("GET /api/v1/blogs/{} - {}: Found blog with title '{}'",
                        id, AppConstants.LogMessages.ENTITY_FOUND, blog.getTitle());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.BLOG_RETRIEVED, blog));
            } else {
                log.warn("GET /api/v1/blogs/{} - {}", id, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/blogs/{} - Error retrieving blog: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve blog: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a blog by its unique slug
     *
     * @param slug Blog slug identifier
     * @return Blog details
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Blog>> getBlogBySlug(@PathVariable String slug) {
        log.info("GET /api/v1/blogs/slug/{} - Request to get blog by slug", slug);

        try {
            Blog blog = blogService.findBySlug(slug);
            if (blog != null) {
                log.info("GET /api/v1/blogs/slug/{} - {}: Found blog with ID {}",
                        slug, AppConstants.LogMessages.ENTITY_FOUND, blog.getId());
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.BLOG_RETRIEVED, blog));
            } else {
                log.warn("GET /api/v1/blogs/slug/{} - {}", slug, AppConstants.LogMessages.ENTITY_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("GET /api/v1/blogs/slug/{} - Error retrieving blog: {}", slug, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve blog: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all unique blog categories
     *
     * @return List of distinct blog categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getBlogCategories() {
        log.info("GET /api/v1/blogs/categories - Request to get blog categories");

        try {
            List<Blog> blogs = blogService.findAll();
            List<String> categories = blogs.stream()
                    .map(Blog::getCategory)
                    .distinct()
                    .filter(category -> category != null && !category.isEmpty())
                    .toList();

            log.info("GET /api/v1/blogs/categories - Success: Retrieved {} unique categories", categories.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CATEGORIES_RETRIEVED, categories));

        } catch (Exception e) {
            log.error("GET /api/v1/blogs/categories - Error retrieving categories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all published blogs (publicly visible)
     *
     * @return List of published blogs
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<Blog>>> getPublishedBlogs() {
        log.info("GET /api/v1/blogs/published - Request to get published blogs");

        try {
            List<Blog> blogs = blogService.findAll();
            List<Blog> publishedBlogs = blogs.stream()
                    .filter(blog -> AppConstants.EntityStatus.PUBLISHED.equalsIgnoreCase(blog.getStatus()))
                    .toList();

            log.info("GET /api/v1/blogs/published - Success: Retrieved {} published blogs from {} total",
                    publishedBlogs.size(), blogs.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.BLOGS_RETRIEVED, publishedBlogs));

        } catch (Exception e) {
            log.error("GET /api/v1/blogs/published - Error retrieving published blogs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve published blogs: " + e.getMessage()));
        }
    }

    /**
     * Retrieves blog statistics grouped by status and category
     *
     * @return Blog statistics data
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlogStatistics() {
        log.info("GET /api/v1/blogs/statistics - Request to get blog statistics");

        try {
            List<Blog> blogs = blogService.findAll();

            long totalBlogs = blogs.size();
            long publishedBlogs = blogs.stream()
                    .filter(blog -> AppConstants.EntityStatus.PUBLISHED.equalsIgnoreCase(blog.getStatus()))
                    .count();
            long draftBlogs = blogs.stream()
                    .filter(blog -> AppConstants.EntityStatus.DRAFT.equalsIgnoreCase(blog.getStatus()))
                    .count();

            Map<String, Object> statistics = Map.of(
                    "totalBlogs", totalBlogs,
                    "publishedBlogs", publishedBlogs,
                    "draftBlogs", draftBlogs,
                    "categories", blogs.stream().map(Blog::getCategory).distinct().count()
            );

            log.info("GET /api/v1/blogs/statistics - Success: Stats calculated - Total: {}, Published: {}, Draft: {}",
                    totalBlogs, publishedBlogs, draftBlogs);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.STATISTICS_RETRIEVED, statistics));

        } catch (Exception e) {
            log.error("GET /api/v1/blogs/statistics - Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve blog statistics: " + e.getMessage()));
        }
    }

    /**
     * Creates a new blog post
     *
     * @param blog Blog data to create
     * @return Created blog details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Blog>> createBlog(@RequestBody Blog blog) {
        log.info("POST /api/v1/blogs - Request to create blog with title '{}'", blog.getTitle());

        try {
            Blog createdBlog = blogService.create(blog);
            log.info("POST /api/v1/blogs - Success: Created blog with ID {} and title '{}'",
                    createdBlog.getId(), createdBlog.getTitle());
            return ResponseEntity.ok(ApiResponse.success("Blog created successfully", createdBlog));

        } catch (Exception e) {
            log.error("POST /api/v1/blogs - Error creating blog with title '{}': {}",
                    blog.getTitle(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create blog: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing blog post
     *
     * @param id   Blog ID
     * @param blog Updated blog data
     * @return Update status
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateBlog(@PathVariable Long id, @RequestBody Blog blog) {
        log.info("PUT /api/v1/blogs/{} - Request to update blog with title '{}'", id, blog.getTitle());

        try {
            blog.setId(id);
            int result = blogService.update(blog);
            if (result > 0) {
                log.info("PUT /api/v1/blogs/{} - Success: Updated {} record(s)", id, result);
                return ResponseEntity.ok(ApiResponse.success("Blog updated successfully", "Updated " + result + " record(s)"));
            } else {
                log.warn("PUT /api/v1/blogs/{} - Blog not found for update", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("PUT /api/v1/blogs/{} - Error updating blog: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update blog: " + e.getMessage()));
        }
    }

    /**
     * Deletes a blog post
     *
     * @param id Blog ID
     * @return Deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable Long id) {
        log.info("DELETE /api/v1/blogs/{} - Request to delete blog", id);

        try {
            int result = blogService.delete(id);
            if (result > 0) {
                log.info("DELETE /api/v1/blogs/{} - Success: Deleted {} record(s)", id, result);
                return ResponseEntity.ok(ApiResponse.success("Blog deleted successfully", "Deleted " + result + " record(s)"));
            } else {
                log.warn("DELETE /api/v1/blogs/{} - Blog not found for deletion", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("DELETE /api/v1/blogs/{} - Error deleting blog: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete blog: " + e.getMessage()));
        }
    }
}
