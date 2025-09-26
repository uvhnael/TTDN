package org.uvhnael.ktal.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Business logic exception for handling domain-specific errors
 * This exception is thrown when business rules are violated
 */
@Slf4j
public class BusinessException extends RuntimeException {

    private String errorCode;
    private Object[] params;

    public BusinessException(String message) {
        super(message);
        log.debug("BusinessException created with message: {}", message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        log.debug("BusinessException created with message: {}, cause: {}", message, cause.getClass().getSimpleName());
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        log.debug("BusinessException created with errorCode: {}, message: {}", errorCode, message);
    }

    public BusinessException(String errorCode, String message, Object... params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
        log.debug("BusinessException created with errorCode: {}, message: {}, params: {}",
                errorCode, message, params != null ? params.length : 0);
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        log.debug("BusinessException created with errorCode: {}, message: {}, cause: {}",
                errorCode, message, cause.getClass().getSimpleName());
    }

    public BusinessException(String errorCode, String message, Throwable cause, Object... params) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = params;
        log.debug("BusinessException created with errorCode: {}, message: {}, cause: {}, params: {}",
                errorCode, message, cause.getClass().getSimpleName(), params != null ? params.length : 0);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }

    /**
     * Factory methods for common business exceptions
     */
    public static BusinessException invalidInput(String message) {
        return new BusinessException("INVALID_INPUT", message);
    }

    public static BusinessException operationNotAllowed(String message) {
        return new BusinessException("OPERATION_NOT_ALLOWED", message);
    }

    public static BusinessException duplicateResource(String resourceType, String identifier) {
        String message = String.format("%s with identifier '%s' already exists", resourceType, identifier);
        return new BusinessException("DUPLICATE_RESOURCE", message, resourceType, identifier);
    }

    public static BusinessException invalidStatus(String currentStatus, String requestedStatus) {
        String message = String.format("Cannot change status from '%s' to '%s'", currentStatus, requestedStatus);
        return new BusinessException("INVALID_STATUS_TRANSITION", message, currentStatus, requestedStatus);
    }

    public static BusinessException resourceInUse(String resourceType, String identifier) {
        String message = String.format("%s '%s' is currently in use and cannot be modified", resourceType, identifier);
        return new BusinessException("RESOURCE_IN_USE", message, resourceType, identifier);
    }

    public static BusinessException validationFailed(String field, String reason) {
        String message = String.format("Validation failed for field '%s': %s", field, reason);
        return new BusinessException("VALIDATION_FAILED", message, field, reason);
    }

    public static BusinessException configurationError(String component, String issue) {
        String message = String.format("Configuration error in component '%s': %s", component, issue);
        return new BusinessException("CONFIGURATION_ERROR", message, component, issue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BusinessException{");
        sb.append("errorCode='").append(errorCode).append('\'');
        sb.append(", message='").append(getMessage()).append('\'');
        if (params != null && params.length > 0) {
            sb.append(", params=").append(java.util.Arrays.toString(params));
        }
        sb.append('}');
        return sb.toString();
    }
}
