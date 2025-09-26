package org.uvhnael.ktal.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Resource not found exception for handling cases where requested resources don't exist
 * This exception is thrown when a requested resource cannot be found in the system
 */
@Slf4j
public class ResourceNotFoundException extends RuntimeException {

    private String resourceType;
    private String resourceId;
    private String errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        log.debug("ResourceNotFoundException created with message: {}", message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        log.debug("ResourceNotFoundException created with message: {}, cause: {}", message, cause.getClass().getSimpleName());
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = "RESOURCE_NOT_FOUND";
        log.debug("ResourceNotFoundException created for resourceType: {}, resourceId: {}", resourceType, resourceId);
    }

    public ResourceNotFoundException(String resourceType, String resourceId, String customMessage) {
        super(customMessage);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = "RESOURCE_NOT_FOUND";
        log.debug("ResourceNotFoundException created for resourceType: {}, resourceId: {}, customMessage: {}",
                resourceType, resourceId, customMessage);
    }

    public ResourceNotFoundException(String errorCode, String resourceType, String resourceId, String message) {
        super(message);
        this.errorCode = errorCode;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        log.debug("ResourceNotFoundException created with errorCode: {}, resourceType: {}, resourceId: {}, message: {}",
                errorCode, resourceType, resourceId, message);
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Factory methods for common resource not found scenarios
     */
    public static ResourceNotFoundException blog(Long id) {
        return new ResourceNotFoundException("Blog", String.valueOf(id));
    }

    public static ResourceNotFoundException blogBySlug(String slug) {
        return new ResourceNotFoundException("Blog", slug, String.format("Blog with slug '%s' not found", slug));
    }

    public static ResourceNotFoundException project(Long id) {
        return new ResourceNotFoundException("Project", String.valueOf(id));
    }

    public static ResourceNotFoundException projectBySlug(String slug) {
        return new ResourceNotFoundException("Project", slug, String.format("Project with slug '%s' not found", slug));
    }

    public static ResourceNotFoundException service(Long id) {
        return new ResourceNotFoundException("Service", String.valueOf(id));
    }

    public static ResourceNotFoundException contact(Long id) {
        return new ResourceNotFoundException("Contact", String.valueOf(id));
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User", String.valueOf(id));
    }

    public static ResourceNotFoundException userByEmail(String email) {
        // Mask email for logging security
        String maskedEmail = email.contains("@") ?
                email.substring(0, 2) + "***@" + email.split("@")[1] : "***";
        return new ResourceNotFoundException("User", maskedEmail, String.format("User with email not found"));
    }

    public static ResourceNotFoundException category(String category) {
        return new ResourceNotFoundException("Category", category, String.format("Category '%s' not found", category));
    }

    public static ResourceNotFoundException file(String filename) {
        return new ResourceNotFoundException("File", filename, String.format("File '%s' not found", filename));
    }

    public static ResourceNotFoundException embedding(String id) {
        return new ResourceNotFoundException("Embedding", id, String.format("Embedding with ID '%s' not found", id));
    }

    /**
     * Factory methods for relationship-based not found scenarios
     */
    public static ResourceNotFoundException relatedResource(String parentType, String parentId, String childType) {
        String message = String.format("No %s found for %s with ID '%s'", childType, parentType, parentId);
        return new ResourceNotFoundException("RELATED_RESOURCE_NOT_FOUND", childType, parentId, message);
    }

    public static ResourceNotFoundException blogsByCategory(String category) {
        return relatedResource("Category", category, "Blogs");
    }

    public static ResourceNotFoundException projectsByArea(String area) {
        return relatedResource("Area", area, "Projects");
    }

    public static ResourceNotFoundException contactsByService(Long serviceId) {
        return relatedResource("Service", String.valueOf(serviceId), "Contacts");
    }

    /**
     * Factory methods for configuration/system resources
     */
    public static ResourceNotFoundException configuration(String configKey) {
        return new ResourceNotFoundException("CONFIGURATION_NOT_FOUND", "Configuration", configKey,
                String.format("Configuration key '%s' not found", configKey));
    }

    public static ResourceNotFoundException template(String templateName) {
        return new ResourceNotFoundException("TEMPLATE_NOT_FOUND", "Template", templateName,
                String.format("Template '%s' not found", templateName));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResourceNotFoundException{");
        if (errorCode != null) {
            sb.append("errorCode='").append(errorCode).append('\'');
        }
        if (resourceType != null) {
            sb.append(", resourceType='").append(resourceType).append('\'');
        }
        if (resourceId != null) {
            sb.append(", resourceId='").append(resourceId).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
