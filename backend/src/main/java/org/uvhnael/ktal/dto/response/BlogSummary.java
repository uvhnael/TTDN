package org.uvhnael.ktal.dto.response;

@lombok.Data
@lombok.Builder
public class BlogSummary {
    private Long id;
    private String title;
    private String slug;
    private String summary;
}