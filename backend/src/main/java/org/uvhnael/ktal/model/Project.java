package org.uvhnael.ktal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private Long id;
    private String title;
    private String year;
    private String area;
    private String content;
    private String status;
    private String createdAt;
    private String updatedAt;
}
