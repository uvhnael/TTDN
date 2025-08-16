package org.uvhnael.ktal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectImage {
    private Long id;
    private Long projectId;
    private String imageUrl;
}
