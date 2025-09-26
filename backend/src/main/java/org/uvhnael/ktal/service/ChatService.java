package org.uvhnael.ktal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uvhnael.ktal.dto.response.BlogSummary;
import org.uvhnael.ktal.dto.response.ChatResponse;
import org.uvhnael.ktal.dto.response.DetailedChatResponse;
import org.uvhnael.ktal.dto.response.DetailedSimilarityResult;
import org.uvhnael.ktal.model.Blog;
import org.uvhnael.ktal.service.MilvusService.SimilarityResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final BlogService blogService;
    private final MilvusService milvusService;
    private final EmbeddingService embeddingService;
    private final OpenAIService openAIService;

    /**
     * Tìm kiếm blog liên quan dựa trên câu hỏi của user
     */
//    public ChatResponse searchAndAnswer(String userQuery, int maxResults) {
//        try {
//            if (userQuery == null || userQuery.trim().isEmpty()) {
//                return ChatResponse.builder()
//                        .query(userQuery)
//                        .answer("Xin lỗi, tôi cần một câu hỏi để có thể tìm kiếm thông tin cho bạn.")
//                        .relatedBlogs(List.of())
//                        .build();
//            }
//
//            // Tìm kiếm các blog tương tự
//            List<Blog> relatedBlogs = blogService.findSimilarBlogs(userQuery.trim(), maxResults);
//
//            if (relatedBlogs.isEmpty()) {
//                return ChatResponse.builder()
//                        .query(userQuery)
//                        .answer("Tôi không tìm thấy thông tin liên quan đến câu hỏi của bạn. Bạn có thể thử với từ khóa khác không?")
//                        .relatedBlogs(List.of())
//                        .build();
//            }
//
//            // Tạo câu trả lời dựa trên các blog tìm được
//            String answer = generateAnswer(userQuery, relatedBlogs);
//
//            return ChatResponse.builder()
//                    .query(userQuery)
//                    .answer(answer)
//                    .relatedBlogs(relatedBlogs.stream()
//                            .map(this::convertToBlogSummary)
//                            .collect(Collectors.toList()))
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error processing chat query: {}", userQuery, e);
//            return ChatResponse.builder()
//                    .query(userQuery)
//                    .answer("Xin lỗi, đã có lỗi xảy ra khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.")
//                    .relatedBlogs(List.of())
//                    .build();
//        }
//    }
    public ChatResponse searchAndAnswer(String userQuery, int maxResults) {
        try {
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return ChatResponse.builder()
                        .query(userQuery)
                        .answer("Xin lỗi, tôi cần một câu hỏi để có thể tìm kiếm thông tin cho bạn.")
                        .relatedBlogs(List.of())
                        .build();
            }

            // 1. Tìm blog liên quan
            List<Blog> relatedBlogs = blogService.findSimilarBlogs(userQuery.trim(), maxResults);

            if (relatedBlogs.isEmpty()) {
                return ChatResponse.builder()
                        .query(userQuery)
                        .answer("Tôi không tìm thấy thông tin liên quan đến câu hỏi của bạn. Bạn có thể thử với từ khóa khác không?")
                        .relatedBlogs(List.of())
                        .build();
            }

            // 2. Ghép context từ blog
            StringBuilder context = new StringBuilder();
            for (Blog blog : relatedBlogs) {
                context.append("Tiêu đề: ").append(blog.getTitle()).append("\n");
                String cleanContent = blog.getContent().replaceAll("<[^>]*>", "");
                context.append(cleanContent).append("\n\n");
            }

            // 3. Tạo prompt cho LLM
            String prompt = "Bạn là một trợ lý AI. Hãy dựa vào ngữ cảnh sau để trả lời câu hỏi 1 cách ngắn gọn, tự nhiên, như giữa 2 người nhắn tin với nhau.\n\n"
                    + "Ngữ cảnh:\n" + context
                    + "\n\nCâu hỏi: " + userQuery
                    + "\n\nCâu trả lời:";

            // 4. Gọi LLM
            String openAIAnswer = openAIService.ask(prompt);

            // 5. Build response
            return ChatResponse.builder()
                    .query(userQuery)
                    .answer(openAIAnswer)
                    .relatedBlogs(relatedBlogs.stream()
                            .map(this::convertToBlogSummary)
                            .collect(Collectors.toList()))
                    .build();

        } catch (Exception e) {
            log.error("Error processing chat query: {}", userQuery, e);
            return ChatResponse.builder()
                    .query(userQuery)
                    .answer("Xin lỗi, đã có lỗi xảy ra khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.")
                    .relatedBlogs(List.of())
                    .build();
        }
    }

    /**
     * Tìm kiếm với thông tin chi tiết về độ tương tự
     */
    public DetailedChatResponse searchWithSimilarityScores(String userQuery, int maxResults) {
        try {
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return DetailedChatResponse.builder()
                        .query(userQuery)
                        .answer("Xin lỗi, tôi cần một câu hỏi để có thể tìm kiếm thông tin cho bạn.")
                        .similarityResults(List.of())
                        .build();
            }

            // Generate embedding cho query
            float[] queryEmbedding = embeddingService.generateEmbedding(userQuery.trim());

            // Tìm kiếm tương tự
            List<SimilarityResult> similarityResults = milvusService.searchSimilar(queryEmbedding, maxResults);

            if (similarityResults.isEmpty()) {
                return DetailedChatResponse.builder()
                        .query(userQuery)
                        .answer("Tôi không tìm thấy thông tin liên quan đến câu hỏi của bạn.")
                        .similarityResults(List.of())
                        .build();
            }

            // Convert similarity results to detailed results
            List<DetailedSimilarityResult> detailedResults = similarityResults.stream()
                    .map(result -> {
                        try {
                            Long blogId = Long.parseLong(result.getId());
                            Blog blog = blogService.findById(blogId).orElse(null);
                            return DetailedSimilarityResult.builder()
                                    .blog(blog != null ? convertToBlogSummary(blog) : null)
                                    .similarityScore(result.getScore())
                                    .matchedText(result.getText())
                                    .build();
                        } catch (NumberFormatException e) {
                            log.warn("Invalid blog ID: {}", result.getId());
                            return null;
                        }
                    })
                    .filter(result -> result != null && result.getBlog() != null)
                    .collect(Collectors.toList());

            // Generate answer
            List<Blog> relatedBlogs = detailedResults.stream()
                    .map(DetailedSimilarityResult::getBlog)
                    .map(summary -> {
                        Blog blog = new Blog();
                        blog.setId(summary.getId());
                        blog.setTitle(summary.getTitle());
                        blog.setContent(summary.getSummary());
                        blog.setSlug(summary.getSlug());
                        return blog;
                    })
                    .collect(Collectors.toList());

            String answer = generateAnswer(userQuery, relatedBlogs);

            return DetailedChatResponse.builder()
                    .query(userQuery)
                    .answer(answer)
                    .similarityResults(detailedResults)
                    .build();

        } catch (Exception e) {
            log.error("Error processing detailed chat query: {}", userQuery, e);
            return DetailedChatResponse.builder()
                    .query(userQuery)
                    .answer("Xin lỗi, đã có lỗi xảy ra khi xử lý câu hỏi của bạn.")
                    .similarityResults(List.of())
                    .build();
        }
    }

    /**
     * Generate answer dựa trên các blog liên quan
     */
    private String generateAnswer(String userQuery, List<Blog> relatedBlogs) {
        if (relatedBlogs.isEmpty()) {
            return "Tôi không tìm thấy thông tin liên quan đến câu hỏi của bạn.";
        }

        StringBuilder answer = new StringBuilder();
        answer.append("Dựa trên thông tin tôi tìm được, tôi có thể trả lời câu hỏi của bạn như sau:\n\n");

        // Tạo tóm tắt từ các blog tìm được
        for (int i = 0; i < Math.min(relatedBlogs.size(), 3); i++) {
            Blog blog = relatedBlogs.get(i);
            answer.append("📝 **").append(blog.getTitle()).append("**\n");

            // Tạo excerpt từ content
            String content = blog.getContent();
            if (content != null && !content.isEmpty()) {
                String cleanContent = content.replaceAll("<[^>]*>", ""); // Remove HTML tags
                String excerpt = cleanContent.length() > 200
                        ? cleanContent.substring(0, 200) + "..."
                        : cleanContent;
                answer.append(excerpt).append("\n\n");
            }
        }

        if (relatedBlogs.size() > 3) {
            answer.append("Và còn ").append(relatedBlogs.size() - 3).append(" bài viết liên quan khác...\n\n");
        }

        answer.append("💡 Bạn có thể xem chi tiết các bài viết liên quan bên dưới để có thêm thông tin.");

        return answer.toString();
    }

    /**
     * Convert Blog to BlogSummary for response
     */
    private BlogSummary convertToBlogSummary(Blog blog) {
        String summary = "";
        if (blog.getContent() != null) {
            String cleanContent = blog.getContent().replaceAll("<[^>]*>", "");
            summary = cleanContent.length() > 150
                    ? cleanContent.substring(0, 150) + "..."
                    : cleanContent;
        }

        return BlogSummary.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .summary(summary)
                .build();
    }


}