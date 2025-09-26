package org.uvhnael.ktal.dto.response;

import java.util.List;

@lombok.Data
@lombok.Builder
public class DetailedChatResponse {
    private String query;
    private String answer;
    private List<DetailedSimilarityResult> similarityResults;
}