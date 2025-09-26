package org.uvhnael.ktal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uvhnael.ktal.model.Blog;
import org.uvhnael.ktal.repository.BlogRepository;
import org.uvhnael.ktal.utils.HtmlCleaner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;
    private final EmbeddingService embeddingService;
    private final MilvusService milvusService;

    public List<Blog> findAll() {
        return blogRepository.findAll();
    }

    public Optional<Blog> findById(Long id) {
        return blogRepository.findById(id);
    }

    public Blog create(Blog blog) {
        log.info("Creating new blog with title: {}", blog.getTitle());

        // Save blog first
        blog.setCreatedAt(LocalDateTime.now().toString());
        blog.setUpdatedAt(LocalDateTime.now().toString());
        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog saved to database with ID: {}", savedBlog.getId());

        try {
            log.debug("Starting embedding generation for blog ID: {}", savedBlog.getId());
            // Generate embedding
            String content = HtmlCleaner.cleanHtml(savedBlog.getContent());
            log.debug("HTML content cleaned, length: {} characters", content.length());

            float[] embedding = embeddingService.generateEmbedding(
                    blog.getTitle() + " " + content
            );
            log.debug("Embedding generated successfully, dimension: {}", embedding.length);

            // Save to Milvus
            milvusService.insertEmbedding(
                    savedBlog.getId().toString(),
                    savedBlog.getTitle() + " " + content,
                    embedding
            );
            log.info("Blog embedding saved to Milvus successfully for blog ID: {}", savedBlog.getId());

        } catch (Exception e) {
            // Log error but don't fail blog creation
            log.error("Error creating embedding for blog ID {}: {}", savedBlog.getId(), e.getMessage(), e);
        }

        log.info("Blog creation completed for ID: {}", savedBlog.getId());
        return savedBlog;
    }

    public int update(Blog blog) {
        log.info("Updating blog with ID: {}", blog.getId());
        blog.setUpdatedAt(LocalDateTime.now().toString());
        blogRepository.update(blog);
        log.info("Blog updated in database with ID: {}", blog.getId());

        try {
            log.debug("Starting embedding update for blog ID: {}", blog.getId());
            // Update embedding
            float[] embedding = embeddingService.generateEmbedding(
                    blog.getTitle() + " " + blog.getContent()
            );
            log.debug("New embedding generated for blog ID: {}", blog.getId());

            // Update in Milvus (delete old + insert new)
            milvusService.deleteEmbedding(blog.getId().toString());
            log.debug("Old embedding deleted from Milvus for blog ID: {}", blog.getId());

            milvusService.insertEmbedding(
                    blog.getId().toString(),
                    blog.getTitle() + " " + blog.getContent(),
                    embedding
            );
            log.info("Blog embedding updated in Milvus successfully for blog ID: {}", blog.getId());
        } catch (Exception e) {
            log.error("Error updating embedding for blog ID {}: {}", blog.getId(), e.getMessage(), e);
        }

        log.info("Blog update completed for ID: {}", blog.getId());
        return 1;
    }

    public int delete(Long id) {
        log.info("Deleting blog with ID: {}", id);

        try {
            // Delete from Milvus first
            milvusService.deleteEmbedding(id.toString());
            log.info("Blog embedding deleted from Milvus for ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting embedding for blog ID {}: {}", id, e.getMessage(), e);
        }

        int result = blogRepository.deleteById(id);
        if (result > 0) {
            log.info("Blog deleted from database successfully for ID: {}", id);
        } else {
            log.warn("No blog found to delete with ID: {}", id);
        }

        return result;
    }

    public Blog findBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    // Add method to search similar blogs
    public List<String> findSimilarContentIds(String query, int limit) {
        log.debug("Searching for similar content with query: '{}', limit: {}", query, limit);

        try {
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            log.debug("Query embedding generated successfully, dimension: {}", queryEmbedding.length);

            List<MilvusService.SimilarityResult> results = milvusService.searchSimilar(queryEmbedding, limit);
            log.info("Found {} similar content results for query: '{}'", results.size(), query);

            return results.stream()
                    .map(MilvusService.SimilarityResult::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching similar content for query '{}': {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Enhanced method to search similar blogs with Blog objects
    public List<Blog> findSimilarBlogs(String query, int limit) {
        log.debug("Searching for similar blogs with query: '{}', limit: {}", query, limit);

        try {
            List<String> similarIds = findSimilarContentIds(query, limit);
            log.debug("Retrieved {} similar blog IDs", similarIds.size());

            List<Blog> similarBlogs = similarIds.stream()
                    .map(Long::parseLong)
                    .map(this::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            log.info("Found {} similar blogs for query: '{}'", similarBlogs.size(), query);
            return similarBlogs;
        } catch (Exception e) {
            log.error("Error finding similar blogs for query '{}': {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}