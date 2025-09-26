package org.uvhnael.ktal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uvhnael.ktal.constants.AppConstants;
import org.uvhnael.ktal.dto.response.ApiResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    /**
     * Upload single file for CKEditor integration
     *
     * @param file File to upload
     * @return Upload result with file URL
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImageForCKEditor(@RequestParam("upload") MultipartFile file) {
        log.info("POST /api/v1/files/upload - {}: File upload request, size: {} bytes",
                AppConstants.LogMessages.FILE_UPLOAD_STARTED, file.getSize());

        try {
            // Create upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                log.debug("Upload directory created: {}, success: {}", uploadDir, created);
            }

            // Validate file type using constants
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedImageType(contentType)) {
                log.warn("POST /api/v1/files/upload - Invalid file type: {}", contentType);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.FILE_TYPE_NOT_SUPPORTED,
                                Map.of("uploaded", false, "error", Map.of("message", "Only image files are allowed"))));
            }

            // Validate file size using constants
            if (file.getSize() > AppConstants.FileUpload.MAX_IMAGE_SIZE) {
                log.warn("POST /api/v1/files/upload - File too large: {} bytes", file.getSize());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.FILE_SIZE_EXCEEDED,
                                Map.of("uploaded", false, "error", Map.of("message",
                                        "File size exceeds maximum limit of " +
                                                (AppConstants.FileUpload.MAX_IMAGE_SIZE / 1024 / 1024) + "MB"))));
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // Validate extension using constants
            if (!isAllowedImageExtension(extension)) {
                log.warn("POST /api/v1/files/upload - Invalid file extension: {}", extension);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(AppConstants.Messages.FILE_TYPE_NOT_SUPPORTED,
                                Map.of("uploaded", false, "error", Map.of("message", "File extension not allowed"))));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/" + fileName;

            Map<String, Object> response = Map.of(
                    "uploaded", true,
                    "fileName", fileName,
                    "url", fileUrl
            );

            log.info("POST /api/v1/files/upload - {}: File uploaded successfully: {}",
                    AppConstants.LogMessages.FILE_UPLOAD_COMPLETED, fileName);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.FILE_UPLOADED, response));

        } catch (IOException e) {
            log.error("POST /api/v1/files/upload - Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage(),
                            Map.of("uploaded", false, "error", Map.of("message", "Upload failed"))));
        }
    }

    /**
     * Upload multiple files
     *
     * @param files Array of files to upload
     * @return List of uploaded file information
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("POST /api/v1/files/upload-multiple - {}: Multiple file upload request, count: {}",
                AppConstants.LogMessages.FILE_UPLOAD_STARTED, files.length);

        try {
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            // Create upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (MultipartFile file : files) {
                try {
                    // Validate each file
                    if (file.getSize() > AppConstants.FileUpload.MAX_FILE_SIZE) {
                        uploadedFiles.add(Map.of(
                                "originalName", file.getOriginalFilename(),
                                "success", false,
                                "error", "File size exceeds limit"
                        ));
                        continue;
                    }

                    String originalFilename = file.getOriginalFilename();
                    String extension = getFileExtension(originalFilename);

                    if (!isAllowedExtension(extension)) {
                        uploadedFiles.add(Map.of(
                                "originalName", originalFilename,
                                "success", false,
                                "error", "File type not allowed"
                        ));
                        continue;
                    }

                    String fileName = UUID.randomUUID().toString() + extension;
                    Path filePath = Paths.get(uploadDir, fileName);
                    Files.copy(file.getInputStream(), filePath);

                    uploadedFiles.add(Map.of(
                            "originalName", originalFilename,
                            "fileName", fileName,
                            "url", "/uploads/" + fileName,
                            "success", true,
                            "size", file.getSize()
                    ));

                } catch (IOException e) {
                    uploadedFiles.add(Map.of(
                            "originalName", file.getOriginalFilename(),
                            "success", false,
                            "error", "Upload failed: " + e.getMessage()
                    ));
                }
            }

            log.info("POST /api/v1/files/upload-multiple - {}: Uploaded {} files",
                    AppConstants.LogMessages.FILE_UPLOAD_COMPLETED,
                    uploadedFiles.stream().mapToLong(f -> (Boolean) f.get("success") ? 1 : 0).sum());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.FILES_RETRIEVED, uploadedFiles));

        } catch (Exception e) {
            log.error("POST /api/v1/files/upload-multiple - Error uploading files: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload files: " + e.getMessage()));
        }
    }

    /**
     * Get list of uploaded files
     *
     * @param limit Maximum number of files to return
     * @return List of file information
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUploadedFiles(
            @RequestParam(defaultValue = "20") int limit) {

        // Validate limit using constants
        if (limit > AppConstants.Defaults.MAX_PAGE_SIZE) {
            limit = AppConstants.Defaults.DEFAULT_FILE_LIST_LIMIT;
        }

        log.info("GET /api/v1/files - Request to get {} uploaded files", limit);

        try {
            File directory = new File(uploadDir);
            List<Map<String, Object>> fileList = new ArrayList<>();

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    Arrays.stream(files)
                            .filter(File::isFile)
                            .limit(limit)
                            .forEach(file -> {
                                fileList.add(Map.of(
                                        "name", file.getName(),
                                        "size", file.length(),
                                        "url", "/uploads/" + file.getName(),
                                        "lastModified", file.lastModified()
                                ));
                            });
                }
            }

            log.info("GET /api/v1/files - Success: Retrieved {} files", fileList.size());
            return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.FILES_RETRIEVED, fileList));

        } catch (Exception e) {
            log.error("GET /api/v1/files - Error retrieving files: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve files: " + e.getMessage()));
        }
    }

    /**
     * Delete a file by filename
     *
     * @param filename Name of file to delete
     * @return Deletion result
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable String filename) {
        log.info("DELETE /api/v1/files/{} - Request to delete file", filename);

        try {
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("DELETE /api/v1/files/{} - Success: File deleted", filename);
                return ResponseEntity.ok(ApiResponse.success(AppConstants.Messages.FILE_DELETED,
                        "File deleted successfully"));
            } else {
                log.warn("DELETE /api/v1/files/{} - File not found", filename);
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            log.error("DELETE /api/v1/files/{} - Error deleting file: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete file: " + e.getMessage()));
        }
    }

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<byte[]> viewFile(@PathVariable String filename) {
        log.info("GET /api/v1/files/view - Request to view file: {}", filename);

        try {
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                        .body(fileBytes);
            } else {
                log.warn("GET /api/v1/files/view - File not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            log.error("GET /api/v1/files/view - Error viewing file {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods for file validation
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }

    private boolean isAllowedImageType(String contentType) {
        return Arrays.asList(AppConstants.FileUpload.IMAGE_MIME_TYPES).contains(contentType);
    }

    private boolean isAllowedImageExtension(String extension) {
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return Arrays.asList(AppConstants.FileUpload.ALLOWED_IMAGE_EXTENSIONS).contains(ext.toLowerCase());
    }

    private boolean isAllowedExtension(String extension) {
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return isAllowedImageExtension(ext) ||
                Arrays.asList(AppConstants.FileUpload.ALLOWED_DOCUMENT_EXTENSIONS).contains(ext.toLowerCase()) ||
                Arrays.asList(AppConstants.FileUpload.ALLOWED_VIDEO_EXTENSIONS).contains(ext.toLowerCase()) ||
                Arrays.asList(AppConstants.FileUpload.ALLOWED_AUDIO_EXTENSIONS).contains(ext.toLowerCase());
    }
}
