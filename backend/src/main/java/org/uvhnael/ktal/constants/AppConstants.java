package org.uvhnael.ktal.constants;

/**
 * Application constants to avoid hardcoding values throughout the project
 * Centralized configuration for all modules: Blogs, Projects, Services, Contacts, Users, Chat, Files
 */
public final class AppConstants {

    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // API Response Messages
    public static final class Messages {
        // Generic messages
        public static final String SUCCESS_RETRIEVED = "Data retrieved successfully";
        public static final String SUCCESS_CREATED = "Data created successfully";
        public static final String SUCCESS_UPDATED = "Data updated successfully";
        public static final String SUCCESS_DELETED = "Data deleted successfully";

        public static final String ERROR_NOT_FOUND = "Resource not found";
        public static final String ERROR_INVALID_INPUT = "Invalid input provided";
        public static final String ERROR_INTERNAL_SERVER = "Internal server error occurred";
        public static final String ERROR_DATABASE_OPERATION = "Database operation failed";
        public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
        public static final String ERROR_FORBIDDEN = "Access forbidden";

        // Blog messages
        public static final String BLOG_RETRIEVED = "Blog retrieved successfully";
        public static final String BLOGS_RETRIEVED = "Blogs retrieved successfully";
        public static final String BLOG_CREATED = "Blog created successfully";
        public static final String BLOG_UPDATED = "Blog updated successfully";
        public static final String BLOG_DELETED = "Blog deleted successfully";
        public static final String BLOG_PUBLISHED = "Blog published successfully";

        // Project messages
        public static final String PROJECT_RETRIEVED = "Project retrieved successfully";
        public static final String PROJECTS_RETRIEVED = "Projects retrieved successfully";
        public static final String PROJECT_CREATED = "Project created successfully";
        public static final String PROJECT_UPDATED = "Project updated successfully";
        public static final String PROJECT_DELETED = "Project deleted successfully";
        public static final String PROJECT_AREAS_RETRIEVED = "Project areas retrieved successfully";
        public static final String PROJECT_YEARS_RETRIEVED = "Project years retrieved successfully";

        // Service messages
        public static final String SERVICE_RETRIEVED = "Service retrieved successfully";
        public static final String SERVICES_RETRIEVED = "Services retrieved successfully";
        public static final String SERVICE_CREATED = "Service created successfully";
        public static final String SERVICE_UPDATED = "Service updated successfully";
        public static final String SERVICE_DELETED = "Service deleted successfully";
        public static final String FEATURED_SERVICES_RETRIEVED = "Featured services retrieved successfully";
        public static final String PRICE_RANGES_RETRIEVED = "Price ranges retrieved successfully";

        // Contact messages
        public static final String CONTACT_RETRIEVED = "Contact retrieved successfully";
        public static final String CONTACTS_RETRIEVED = "Contacts retrieved successfully";
        public static final String CONTACT_CREATED = "Contact created successfully";
        public static final String CONTACT_UPDATED = "Contact updated successfully";
        public static final String CONTACT_DELETED = "Contact deleted successfully";
        public static final String CONTACT_NOTE_UPDATED = "Contact note updated successfully";
        public static final String CONTACT_STATUS_UPDATED = "Contact status updated successfully";
        public static final String PENDING_CONTACTS_RETRIEVED = "Pending contacts retrieved successfully";

        // User messages
        public static final String USER_RETRIEVED = "User retrieved successfully";
        public static final String USERS_RETRIEVED = "Users retrieved successfully";
        public static final String USER_CREATED = "User created successfully";
        public static final String USER_UPDATED = "User updated successfully";
        public static final String USER_DELETED = "User deleted successfully";
        public static final String USER_LOGIN_SUCCESS = "User logged in successfully";
        public static final String USER_LOGOUT_SUCCESS = "User logged out successfully";
        public static final String USER_PROFILE_UPDATED = "User profile updated successfully";
        public static final String USER_PASSWORD_UPDATED = "Password updated successfully";

        // Chat messages
        public static final String CHAT_RESPONSE_GENERATED = "Chat response generated successfully";
        public static final String CHAT_HISTORY_RETRIEVED = "Chat history retrieved successfully";
        public static final String CHAT_SESSION_CREATED = "Chat session created successfully";
        public static final String CHAT_SESSION_ENDED = "Chat session ended successfully";
        public static final String SIMILAR_CONTENT_FOUND = "Similar content found successfully";

        // File upload messages
        public static final String FILE_UPLOADED = "File uploaded successfully";
        public static final String FILE_DELETED = "File deleted successfully";
        public static final String FILE_RETRIEVED = "File retrieved successfully";
        public static final String FILES_RETRIEVED = "Files retrieved successfully";
        public static final String FILE_TYPE_NOT_SUPPORTED = "File type not supported";
        public static final String FILE_SIZE_EXCEEDED = "File size exceeded maximum limit";

        // Dashboard messages
        public static final String DASHBOARD_OVERVIEW_RETRIEVED = "Dashboard overview retrieved successfully";
        public static final String RECENT_ACTIVITIES_RETRIEVED = "Recent activities retrieved successfully";
        public static final String CONTENT_STATS_RETRIEVED = "Content statistics retrieved successfully";
        public static final String CONTACT_ANALYSIS_RETRIEVED = "Contact analysis retrieved successfully";
        public static final String GLOBAL_SEARCH_COMPLETED = "Global search completed successfully";

        // General utility messages
        public static final String CATEGORIES_RETRIEVED = "Categories retrieved successfully";
        public static final String STATISTICS_RETRIEVED = "Statistics retrieved successfully";
        public static final String HEALTH_CHECK_SUCCESS = "System health check passed";
        public static final String BACKUP_CREATED = "Backup created successfully";
        public static final String RESTORE_COMPLETED = "Restore completed successfully";
    }

    // API Status Values
    public static final class Status {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String WARNING = "warning";
        public static final String INFO = "info";
    }

    // Entity Status Values
    public static final class EntityStatus {
        // Blog statuses
        public static final String PUBLISHED = "published";
        public static final String DRAFT = "draft";
        public static final String ARCHIVED = "archived";
        public static final String SCHEDULED = "scheduled";

        // Project statuses
        public static final String ACTIVE = "active";
        public static final String INACTIVE = "inactive";
        public static final String COMPLETED = "completed";
        public static final String ON_HOLD = "on_hold";
        public static final String CANCELLED = "cancelled";

        // Contact statuses
        public static final String PENDING = "pending";
        public static final String HANDLED = "handled";
        public static final String CLOSED = "closed";
        public static final String IN_PROGRESS = "in_progress";

        // User statuses
        public static final String USER_ACTIVE = "active";
        public static final String USER_INACTIVE = "inactive";
        public static final String USER_SUSPENDED = "suspended";
        public static final String USER_PENDING_VERIFICATION = "pending_verification";

        // File statuses
        public static final String FILE_UPLOADED = "uploaded";
        public static final String FILE_PROCESSING = "processing";
        public static final String FILE_READY = "ready";
        public static final String FILE_ERROR = "error";
    }

    // Default Values
    public static final class Defaults {
        // Pagination
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int MIN_PAGE_SIZE = 1;

        // Limits
        public static final int DEFAULT_RECENT_LIMIT = 10;
        public static final int DEFAULT_FEATURED_LIMIT = 6;
        public static final int DEFAULT_SEARCH_LIMIT = 10;
        public static final int MAX_SEARCH_RESULTS = 50;

        // Entity defaults
        public static final String DEFAULT_CONTACT_STATUS = "pending";
        public static final String DEFAULT_USER_STATUS = "active";
        public static final String DEFAULT_PROJECT_STATUS = "active";
        public static final String DEFAULT_BLOG_STATUS = "draft";

        // Sorting
        public static final String DEFAULT_SORT_DIRECTION = "desc";
        public static final String DEFAULT_SORT_FIELD = "createdAt";

        // Chat defaults
        public static final int DEFAULT_CHAT_HISTORY_LIMIT = 20;
        public static final int MAX_CHAT_MESSAGE_LENGTH = 2000;
        public static final int DEFAULT_SIMILARITY_LIMIT = 5;

        // File defaults
        public static final int DEFAULT_FILE_LIST_LIMIT = 20;
        public static final String DEFAULT_FILE_SORT = "uploadDate";
    }

    // Log Messages
    public static final class LogMessages {
        public static final String REQUEST_RECEIVED = "Request received";
        public static final String REQUEST_PROCESSED = "Request processed successfully";
        public static final String REQUEST_FAILED = "Request processing failed";

        public static final String DATA_FILTERED = "Applied filter";
        public static final String DATA_SORTED = "Applied sorting";
        public static final String DATA_PAGINATED = "Applied pagination";
        public static final String DATA_VALIDATED = "Data validation completed";

        public static final String ENTITY_FOUND = "Entity found";
        public static final String ENTITY_NOT_FOUND = "Entity not found";
        public static final String ENTITY_CREATED = "Entity created";
        public static final String ENTITY_UPDATED = "Entity updated";
        public static final String ENTITY_DELETED = "Entity deleted";

        public static final String USER_AUTHENTICATED = "User authenticated";
        public static final String USER_AUTHORIZATION_FAILED = "User authorization failed";
        public static final String FILE_UPLOAD_STARTED = "File upload started";
        public static final String FILE_UPLOAD_COMPLETED = "File upload completed";
        public static final String CHAT_SESSION_INITIATED = "Chat session initiated";
        public static final String SIMILARITY_SEARCH_PERFORMED = "Similarity search performed";
    }

    // Validation Constants
    public static final class Validation {
        // Length constraints
        public static final int MIN_TITLE_LENGTH = 3;
        public static final int MAX_TITLE_LENGTH = 255;
        public static final int MAX_CONTENT_LENGTH = 50000;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final int MAX_NOTE_LENGTH = 2000;
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int MAX_USERNAME_LENGTH = 50;
        public static final int MIN_USERNAME_LENGTH = 3;

        // Project specific
        public static final int MIN_PROJECT_YEAR = 1900;
        public static final int MAX_PROJECT_YEAR = 2100;
        public static final int MAX_PROJECT_AREA_LENGTH = 100;

        // Contact specific
        public static final int MAX_CONTACT_NAME_LENGTH = 100;
        public static final int MAX_CONTACT_MESSAGE_LENGTH = 2000;

        // Chat specific
        public static final int MAX_CHAT_MESSAGE_LENGTH = 2000;
        public static final int MIN_CHAT_MESSAGE_LENGTH = 1;

        // Regular expressions
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
        public static final String SLUG_REGEX = "^[a-z0-9-]+$";
        public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]+$";
        public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";
    }

    // File Upload Constants
    public static final class FileUpload {
        public static final String UPLOAD_DIR = "uploads/";
        public static final String TEMP_DIR = "temp/";
        public static final String BACKUP_DIR = "backups/";

        // File size limits (in bytes)
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
        public static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB
        public static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

        // Allowed file extensions
        public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"};
        public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf", "doc", "docx", "txt", "rtf", "odt"};
        public static final String[] ALLOWED_VIDEO_EXTENSIONS = {"mp4", "avi", "mov", "wmv", "flv", "webm"};
        public static final String[] ALLOWED_AUDIO_EXTENSIONS = {"mp3", "wav", "ogg", "aac", "m4a"};

        // MIME types
        public static final String[] IMAGE_MIME_TYPES = {
                "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg+xml"
        };
        public static final String[] DOCUMENT_MIME_TYPES = {
                "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain", "application/rtf", "application/vnd.oasis.opendocument.text"
        };
    }

    // Security Constants
    public static final class Security {
        public static final String JWT_SECRET_KEY = "ktal_jwt_secret_key_2025";
        public static final int JWT_EXPIRATION_HOURS = 24;
        public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int LOCKOUT_DURATION_MINUTES = 30;

        // Password policy
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final boolean REQUIRE_UPPERCASE = true;
        public static final boolean REQUIRE_LOWERCASE = true;
        public static final boolean REQUIRE_NUMBERS = true;
        public static final boolean REQUIRE_SPECIAL_CHARS = true;

        // Session management
        public static final int SESSION_TIMEOUT_MINUTES = 120;
        public static final int CSRF_TOKEN_VALIDITY_MINUTES = 60;
    }

    // Cache Constants
    public static final class Cache {
        public static final String BLOG_CACHE = "blogs";
        public static final String PROJECT_CACHE = "projects";
        public static final String SERVICE_CACHE = "services";
        public static final String USER_CACHE = "users";
        public static final String CATEGORY_CACHE = "categories";

        // Cache TTL (Time To Live) in seconds
        public static final int BLOG_CACHE_TTL = 300; // 5 minutes
        public static final int PROJECT_CACHE_TTL = 600; // 10 minutes
        public static final int SERVICE_CACHE_TTL = 1800; // 30 minutes
        public static final int USER_CACHE_TTL = 900; // 15 minutes
        public static final int STATISTICS_CACHE_TTL = 180; // 3 minutes
    }

    // Chat/AI Constants
    public static final class Chat {
        public static final int MAX_CONVERSATION_HISTORY = 50;
        public static final int DEFAULT_RESPONSE_TIMEOUT = 30; // seconds
        public static final float SIMILARITY_THRESHOLD = 0.7f;
        public static final int MAX_SIMILAR_RESULTS = 5;
        public static final int MAX_CONTEXT_LENGTH = 4000;
        public static final String DEFAULT_MODEL = "gpt-3.5-turbo";
        public static final int MAX_TOKENS = 1000;
        public static final float TEMPERATURE = 0.7f;
    }

    // Database Constants
    public static final class Database {
        public static final int CONNECTION_POOL_SIZE = 20;
        public static final int MAX_RETRY_ATTEMPTS = 3;
        public static final int QUERY_TIMEOUT_SECONDS = 30;
        public static final int BATCH_SIZE = 100;

        // Table names
        public static final String BLOG_TABLE = "blog";
        public static final String PROJECT_TABLE = "project";
        public static final String SERVICE_TABLE = "service";
        public static final String CONTACT_TABLE = "contact";
        public static final String USER_TABLE = "user";
    }

    // API Rate Limiting
    public static final class RateLimit {
        public static final int REQUESTS_PER_MINUTE = 100;
        public static final int CHAT_REQUESTS_PER_MINUTE = 20;
        public static final int FILE_UPLOAD_REQUESTS_PER_MINUTE = 10;
        public static final int SEARCH_REQUESTS_PER_MINUTE = 50;
    }

    // Notification Constants
    public static final class Notifications {
        public static final String NEW_CONTACT_SUBJECT = "New Contact Form Submission";
        public static final String CONTACT_STATUS_UPDATED = "Contact Status Updated";
        public static final String PROJECT_STATUS_CHANGED = "Project Status Changed";
        public static final String BLOG_PUBLISHED = "New Blog Post Published";
        public static final String USER_ACCOUNT_CREATED = "Account Created Successfully";
        public static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
    }
}
