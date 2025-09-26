package org.uvhnael.ktal.dto.response;

import java.util.List;

@lombok.Data
@lombok.Builder
public class ChatResponse {
    private String query;
    private String answer;
    private List<BlogSummary> relatedBlogs;
}