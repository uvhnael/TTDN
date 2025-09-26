package org.uvhnael.ktal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@lombok.Data
public class ChatRequest {
    @NotBlank(message = "Query không được để trống")
    private String query;

    @Min(value = 1, message = "MaxResults phải lớn hơn 0")
    @Max(value = 20, message = "MaxResults không được vượt quá 20")
    private Integer maxResults = 5;
}