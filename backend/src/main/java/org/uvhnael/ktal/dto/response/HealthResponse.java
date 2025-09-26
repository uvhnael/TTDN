package org.uvhnael.ktal.dto.response;

@lombok.Data
@lombok.Builder
public class HealthResponse {
    private String status;
    private String message;
    private java.time.LocalDateTime timestamp;
}