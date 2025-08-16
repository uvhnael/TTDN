package org.uvhnael.ktal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class FileContronller {
    // Upload multiple files and return file URLs

    // Define upload directory - using a temp directory that can be overridden in application.properties
    @Value("${file.upload-dir:${java.io.tmpdir}}")
    private String uploadDir;

    // CKEditor upload endpoint
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImageForCKEditor(@RequestParam("upload") MultipartFile file) {
        try {
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Kiểm tra file có phải ảnh không
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "uploaded", false,
                        "error", Map.of("message", "Only image files are allowed")
                ));
            }

            // Tạo tên file mới để tránh trùng
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.write(filePath, file.getBytes());

            // Trả đúng format CKEditor yêu cầu
            return ResponseEntity.ok(Map.of(
                    "uploaded", true,
                    "url", "http://localhost:1707/api/v1/file/view/" + newFilename
            ));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "uploaded", false,
                    "error", Map.of("message", "File upload failed: " + e.getMessage())
            ));
        }
    }


    @PostMapping("/file/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();

        try {
            // Create directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Process each file
            for (MultipartFile file : files) {
                // Generate unique filename to prevent conflicts
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;

                // Save the file
                Path filePath = Paths.get(uploadDir, newFilename);
                Files.write(filePath, file.getBytes());

                // Generate URL for the file
                String fileUrl = "/api/v1/file/view/" + newFilename;
                fileUrls.add(fileUrl);
            }

            return ResponseEntity.ok(fileUrls);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/file/view/{filename:.+}")
    public ResponseEntity<byte[]> viewFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            byte[] fileContent = Files.readAllBytes(filePath);

            // Lấy loại MIME từ tên file
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", mimeType)
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(fileContent);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
