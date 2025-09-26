package org.uvhnael.ktal.dto.response;

@lombok.Data
@lombok.Builder
public class ErrorResponse {
    private String error;
    private String message;
    private java.time.LocalDateTime timestamp;
}