package org.uvhnael.ktal.dto.response;

@lombok.Data
@lombok.Builder
public class DetailedSimilarityResult {
    private BlogSummary blog;
    private float similarityScore;
    private String matchedText;
}