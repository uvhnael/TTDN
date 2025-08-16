package org.uvhnael.ktal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {
    private Long id;
    private String icon;
    private String title;
    private String description;
    private String features;
    private String price;
}
