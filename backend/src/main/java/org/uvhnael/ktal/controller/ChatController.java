package org.uvhnael.ktal.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.request.ChatRequest;
import org.uvhnael.ktal.dto.response.ApiResponse;
import org.uvhnael.ktal.dto.response.ChatResponse;
import org.uvhnael.ktal.dto.response.DetailedChatResponse;
import org.uvhnael.ktal.service.ChatService;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Main chat endpoint - search and answer questions
     *
     * @param request Chat request containing query and optional parameters
     * @return Chat response with answer and related content
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatResponse>> askQuestion(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/v1/chat/ask - {}: Chat request with query length: {} chars",
                AppConstants.LogMessages.CHAT_SESSION_INITIATED,
                request.getQuery() != null ? request.getQuery().length() : 0);

        try {
            // Validate message length using constants
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                log.warn("POST /api/v1/chat/ask - Empty query provided");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.ERROR_INVALID_INPUT));
            }

            if (request.getQuery().length() > AppConstants.Chat.MAX_CONTEXT_LENGTH) {
                log.warn("POST /api/v1/chat/ask - Query too long: {} chars", request.getQuery().length());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Query exceeds maximum length of " +
                                AppConstants.Chat.MAX_CONTEXT_LENGTH + " characters"));
            }

            // Use constants for max results
            int maxResults = request.getMaxResults() != null ?
                    request.getMaxResults() : AppConstants.Chat.MAX_SIMILAR_RESULTS;

            if (maxResults > AppConstants.Defaults.MAX_SEARCH_RESULTS) {
                maxResults = AppConstants.Chat.MAX_SIMILAR_RESULTS;
            }

            ChatResponse response = chatService.searchAndAnswer(request.getQuery(), maxResults);

            log.info("POST /api/v1/chat/ask - Success: Generated response for query with {} related items",
                    response.getRelatedBlogs() != null ? response.getRelatedBlogs().size() : 0);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CHAT_RESPONSE_GENERATED, response));

        } catch (Exception e) {
            log.error("POST /api/v1/chat/ask - Error processing chat request: {}", e.getMessage(), e);

            ChatResponse errorResponse = ChatResponse.builder()
                    .query(request.getQuery())
                    .answer("Xin lỗi, đã có lỗi xảy ra khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.")
                    .relatedBlogs(java.util.List.of())
                    .build();

            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to process chat request: " + e.getMessage(), errorResponse));
        }
    }

    /**
     * Detailed chat endpoint with comprehensive response
     *
     * @param request Chat request
     * @return Detailed chat response with similarity scores and metadata
     */
    @PostMapping("/detailed")
    public ResponseEntity<ApiResponse<DetailedChatResponse>> askDetailedQuestion(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/v1/chat/detailed - {}: Detailed chat request",
                AppConstants.LogMessages.CHAT_SESSION_INITIATED);

        try {
            // Apply the same validation as regular chat
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.ERROR_INVALID_INPUT));
            }

            if (request.getQuery().length() > AppConstants.Chat.MAX_CONTEXT_LENGTH) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Query exceeds maximum length"));
            }

            int maxResults = request.getMaxResults() != null ?
                    request.getMaxResults() : AppConstants.Chat.MAX_SIMILAR_RESULTS;

            DetailedChatResponse response = chatService.searchWithSimilarityScores(request.getQuery(), maxResults);

            log.info("POST /api/v1/chat/detailed - Success: Generated detailed response");
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.CHAT_RESPONSE_GENERATED, response));

        } catch (Exception e) {
            log.error("POST /api/v1/chat/detailed - Error processing detailed chat request: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to process detailed chat request: " + e.getMessage()));
        }
    }

    /**
     * Get chat service statistics and configuration
     *
     * @return Chat service statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getChatStatistics() {
        log.info("GET /api/v1/chat/statistics - Chat statistics request");

        try {
            var statistics = java.util.Map.of(
                    "maxContextLength", AppConstants.Chat.MAX_CONTEXT_LENGTH,
                    "maxSimilarResults", AppConstants.Chat.MAX_SIMILAR_RESULTS,
                    "similarityThreshold", AppConstants.Chat.SIMILARITY_THRESHOLD,
                    "defaultModel", AppConstants.Chat.DEFAULT_MODEL,
                    "maxTokens", AppConstants.Chat.MAX_TOKENS,
                    "temperature", AppConstants.Chat.TEMPERATURE,
                    "requestsPerMinute", AppConstants.RateLimit.CHAT_REQUESTS_PER_MINUTE
            );

            log.info("GET /api/v1/chat/statistics - Success: Retrieved chat configuration");
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.STATISTICS_RETRIEVED, statistics));

        } catch (Exception e) {
            log.error("GET /api/v1/chat/statistics - Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve chat statistics: " + e.getMessage()));
        }
    }

}