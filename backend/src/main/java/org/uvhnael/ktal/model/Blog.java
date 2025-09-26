package org.uvhnael.ktal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog {
    private Long id;
    private String title;
    private String slug;
    private String author;
    private String category;
    private String thumbnail;
    private String content;
    private String status;
    private String createdAt;
    private String updatedAt;
}
