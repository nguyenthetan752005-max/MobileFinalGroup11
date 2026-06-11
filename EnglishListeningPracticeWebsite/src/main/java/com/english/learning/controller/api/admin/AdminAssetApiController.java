package com.english.learning.controller.api.admin;

import com.english.learning.service.integration.media.MediaStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAssetApiController {

    private final MediaStorageGateway mediaStorageGateway;

    @PostMapping("/uploads")
    public ResponseEntity<Map<String, Object>> uploadAsset(@RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "resourceType", defaultValue = "auto") String resourceType,
                                                           @RequestParam(value = "folder", required = false) String folder,
                                                           @RequestParam(value = "publicId", required = false) String publicId,
                                                           @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        if (file.isEmpty()) {
            return AdminApiResponses.badRequest("File upload khong duoc de trong.");
        }

        return AdminApiResponses.entity(() -> {
            if (publicId != null && !publicId.isBlank()) {
                return mediaStorageGateway.uploadFile(file, resourceType, folder, publicId, overwrite);
            }
            return mediaStorageGateway.uploadFile(file, resourceType, folder);
        }, "Tai file len thanh cong.");
    }
}
