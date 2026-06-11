package com.english.learning.service.integration.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ImageKitService implements MediaStorageGateway {

    private static final String STORAGE_KEY_PREFIX = "imagekit:";
    private static final String STORAGE_KEY_SEPARATOR = "|";
    private static final String DEFAULT_UPLOAD_NAME = "upload";
    private static final String DEFAULT_AUDIO_NAME = "speaking-audio.wav";
    private static final String MANAGEMENT_FILES_PATH = "/v1/files";
    private static final String UPLOAD_FILE_PATH = "/api/v1/files/upload";

    private final RestClient managementClient;
    private final RestClient uploadClient;
    private final ObjectMapper objectMapper;
    private final String urlEndpoint;
    private final String imageBasePath;
    private final String audioLessonsPath;   // Audio mẫu cho bài học
    private final String audioSpeakingPath;  // Audio nói của người dùng

    public ImageKitService(
            @Qualifier("imageKitManagementClient") RestClient managementClient,
            @Qualifier("imageKitUploadClient") RestClient uploadClient,
            ObjectMapper objectMapper,
            @Value("${imagekit.url-endpoint:}") String urlEndpoint,
            @Value("${imagekit.image-base-path:}") String imageBasePath,
            @Value("${imagekit.audio-lessons-path:EnglishListeningData/AudioLessons}") String audioLessonsPath,
            @Value("${imagekit.audio-speaking-path:EnglishListeningData/AudioSpeaking}") String audioSpeakingPath
    ) {
        this.managementClient = managementClient;
        this.uploadClient = uploadClient;
        this.objectMapper = objectMapper;
        this.urlEndpoint = normalizeBlank(urlEndpoint);
        this.imageBasePath = normalizeBlank(imageBasePath);
        this.audioLessonsPath = normalizeBlank(audioLessonsPath);
        this.audioSpeakingPath = normalizeBlank(audioSpeakingPath);
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "auto", null);
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder) throws IOException {
        return uploadFile(file, resourceType, folder, null, false);
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder, String publicId, boolean overwrite) throws IOException {
        try {
            String effectiveFolder = prependImageBasePath(folder);
            UploadTarget target = resolveUploadTarget(effectiveFolder, publicId, overwrite, file.getOriginalFilename());
            return uploadBinary(file.getBytes(), file.getContentType(), target);
        } catch (RestClientResponseException e) {
            log.error("ImageKit upload failed for folder={} publicId={}: {}", folder, publicId, extractErrorMessage(e));
            throw new IOException("Khong the tai file len ImageKit: " + extractErrorMessage(e), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("ImageKit upload failed for folder={} publicId={}: {}", folder, publicId, e.getMessage());
            throw new IOException("Khong the tai file len ImageKit: " + e.getMessage(), e);
        }
    }

    private String prependImageBasePath(String folder) {
        if (imageBasePath == null || imageBasePath.isBlank()) {
            return folder;
        }
        if (folder == null || folder.isBlank()) {
            return imageBasePath;
        }
        return imageBasePath + "/" + folder;
    }

    @Override
    public Map<String, String> uploadAudio(byte[] audioData, String publicId) throws IOException {
        // Default to speaking path for backward compatibility
        return uploadAudio(audioData, publicId, "speaking");
    }

    @Override
    public Map<String, String> uploadAudio(byte[] audioData, String publicId, String audioType) throws IOException {
        try {
            // Chọn path dựa trên audioType
            String audioFolder;
            if ("lessons".equalsIgnoreCase(audioType)) {
                audioFolder = audioLessonsPath != null ? audioLessonsPath : "EnglishListeningData/AudioLessons";
            } else {
                // Default to speaking path
                audioFolder = audioSpeakingPath != null ? audioSpeakingPath : "EnglishListeningData/AudioSpeaking";
            }
            UploadTarget target = resolveUploadTarget(audioFolder, publicId, true, DEFAULT_AUDIO_NAME);
            return uploadBinary(audioData, "audio/wav", target);
        } catch (RestClientResponseException e) {
            log.error("ImageKit audio upload failed for type={} publicId={}: {}", audioType, publicId, extractErrorMessage(e));
            throw new IOException("Khong the tai audio len ImageKit: " + extractErrorMessage(e), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("ImageKit audio upload failed for type={} publicId={}: {}", audioType, publicId, e.getMessage());
            throw new IOException("Khong the tai audio len ImageKit: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId) throws Exception {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        String fileId = resolveFileId(publicId);
        if (fileId == null || fileId.isBlank()) {
            log.warn("ImageKit: khong resolve duoc fileId tu storage key {}", publicId);
            return;
        }

        try {
            managementClient.delete()
                    .uri(MANAGEMENT_FILES_PATH + "/{fileId}", fileId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            String message = extractErrorMessage(e).toLowerCase();
            if (message.contains("not found") || message.contains("404")) {
                log.warn("ImageKit: fileId {} khong ton tai hoac da bi xoa truoc do.", fileId);
                return;
            }
            throw new Exception("Loi xoa file tren ImageKit: " + extractErrorMessage(e), e);
        } catch (Exception e) {
            throw new Exception("Loi xoa file tren ImageKit: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUrlEndpoint() {
        return urlEndpoint;
    }

    private Map<String, String> uploadBinary(byte[] payload, String contentType, UploadTarget target) throws IOException {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", createFilePart(payload, target.fileName(), contentType));
        body.add("fileName", target.fileName());
        body.add("useUniqueFileName", Boolean.toString(!target.overwriteExisting()));
        body.add("overwriteFile", Boolean.toString(target.overwriteExisting()));
        if (target.overwriteExisting()) {
            body.add("overwriteAITags", "false");
            body.add("overwriteTags", "false");
            body.add("overwriteCustomMetadata", "false");
        }
        if (target.folder() != null) {
            body.add("folder", toApiFolder(target.folder()));
        }

        ImageKitUploadResponse response = uploadClient.post()
                .uri(UPLOAD_FILE_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(ImageKitUploadResponse.class);

        if (response == null || !StringUtils.hasText(response.fileId()) || !StringUtils.hasText(response.url())) {
            throw new IOException("ImageKit khong tra ve thong tin file hop le.");
        }

        return toUploadResult(response);
    }

    private HttpEntity<ByteArrayResource> createFilePart(byte[] payload, String fileName, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("file", fileName);
        String normalizedContentType = normalizeBlank(contentType);
        if (normalizedContentType != null) {
            try {
                headers.setContentType(MediaType.parseMediaType(normalizedContentType));
            } catch (InvalidMediaTypeException ignored) {
                log.debug("ImageKit: bo qua contentType khong hop le {}", normalizedContentType);
            }
        }
        return new HttpEntity<>(new NamedByteArrayResource(payload, fileName), headers);
    }

    private Map<String, String> toUploadResult(ImageKitUploadResponse result) {
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("url", result.url());
        uploadResult.put("publicId", encodeStorageKey(result.fileId(), normalizeFilePath(result.filePath())));
        return uploadResult;
    }

    private UploadTarget resolveUploadTarget(String folder, String storageKey, boolean overwrite, String originalFilename) {
        String normalizedFolder = normalizeFolder(folder);
        String existingFilePath = overwrite ? resolveExistingFilePath(storageKey) : null;

        if (existingFilePath != null) {
            String normalizedPath = ensureExtension(existingFilePath, originalFilename);
            return new UploadTarget(
                    normalizeFolder(extractFolderPath(normalizedPath)),
                    extractFileName(normalizedPath),
                    true
            );
        }

        String requestedName = extractRequestedName(storageKey);
        String fileName = ensureExtension(requestedName != null ? requestedName : safeOriginalFilename(originalFilename), originalFilename);
        return new UploadTarget(normalizedFolder, fileName, overwrite && requestedName != null);
    }

    private String resolveExistingFilePath(String storageKey) {
        StoragePointer pointer = parseStorageKey(storageKey);
        if (pointer.filePath() != null) {
            return pointer.filePath();
        }
        if (pointer.fileId() != null) {
            try {
                ImageKitFileDetails result = managementClient.get()
                        .uri(MANAGEMENT_FILES_PATH + "/{fileId}/details", pointer.fileId())
                        .retrieve()
                        .body(ImageKitFileDetails.class);
                return result != null ? normalizeFilePath(result.filePath()) : null;
            } catch (RestClientResponseException e) {
                log.debug("ImageKit: khong the lay filePath tu fileId {}: {}", pointer.fileId(), extractErrorMessage(e));
            } catch (Exception e) {
                log.debug("ImageKit: khong the lay filePath tu fileId {}: {}", pointer.fileId(), e.getMessage());
            }
        }
        return null;
    }

    private String resolveFileId(String storageKey) {
        StoragePointer pointer = parseStorageKey(storageKey);
        if (pointer.fileId() != null && !pointer.fileId().isBlank()) {
            return pointer.fileId();
        }
        if (pointer.filePath() == null || pointer.filePath().isBlank()) {
            return null;
        }
        return findFileIdByPath(pointer.filePath());
    }

    private String findFileIdByPath(String rawFilePath) {
        String normalizedPath = normalizeFilePath(rawFilePath);
        if (normalizedPath == null) {
            return null;
        }

        try {
            String folderPath = extractFolderPath(normalizedPath);
            ImageKitFileSummary[] files = managementClient.get()
                    .uri(uriBuilder -> {
                        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uriBuilder.build())
                                .replacePath(MANAGEMENT_FILES_PATH)
                                .replaceQuery(null)
                                .queryParam("limit", 1000)
                                .queryParam("type", "file");
                        String apiPath = toApiPath(folderPath);
                        if (apiPath != null) {
                            builder.queryParam("path", apiPath);
                        }
                        return builder.build(true).toUri();
                    })
                    .retrieve()
                    .body(ImageKitFileSummary[].class);
            if (files == null || files.length == 0) {
                return null;
            }

            String normalizedNoExtension = stripExtension(normalizedPath);
            for (ImageKitFileSummary file : files) {
                String candidatePath = normalizeFilePath(file.filePath());
                if (candidatePath == null) {
                    continue;
                }
                if (candidatePath.equals(normalizedPath) || stripExtension(candidatePath).equals(normalizedNoExtension)) {
                    return file.fileId();
                }
            }
        } catch (RestClientResponseException e) {
            log.warn("ImageKit: khong the tim fileId theo path {}: {}", normalizedPath, extractErrorMessage(e));
        } catch (Exception e) {
            log.warn("ImageKit: khong the tim fileId theo path {}: {}", normalizedPath, e.getMessage());
        }

        return null;
    }

    private StoragePointer parseStorageKey(String storageKey) {
        String normalized = normalizeBlank(storageKey);
        if (normalized == null) {
            return new StoragePointer(null, null);
        }
        if (normalized.startsWith(STORAGE_KEY_PREFIX)) {
            String payload = normalized.substring(STORAGE_KEY_PREFIX.length());
            int separatorIndex = payload.indexOf(STORAGE_KEY_SEPARATOR);
            if (separatorIndex < 0) {
                return new StoragePointer(normalizeBlank(payload), null);
            }
            String fileId = normalizeBlank(payload.substring(0, separatorIndex));
            String filePath = normalizeFilePath(payload.substring(separatorIndex + STORAGE_KEY_SEPARATOR.length()));
            return new StoragePointer(fileId, filePath);
        }
        if (looksLikePathOrUrl(normalized)) {
            return new StoragePointer(null, normalizeFilePath(normalized));
        }
        return new StoragePointer(normalized, null);
    }

    private String encodeStorageKey(String fileId, String filePath) {
        if (fileId == null || fileId.isBlank()) {
            return null;
        }
        if (filePath == null || filePath.isBlank()) {
            return fileId;
        }
        return STORAGE_KEY_PREFIX + fileId + STORAGE_KEY_SEPARATOR + filePath;
    }

    private String extractRequestedName(String storageKey) {
        StoragePointer pointer = parseStorageKey(storageKey);
        if (pointer.filePath() != null) {
            return extractFileName(pointer.filePath());
        }
        if (pointer.fileId() != null && !pointer.fileId().isBlank()) {
            return pointer.fileId();
        }
        return null;
    }

    private String normalizeFilePath(String rawValue) {
        String normalized = normalizeBlank(rawValue);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replace('\\', '/');
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            normalized = extractFilePathFromUrl(normalized);
        }
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        normalized = normalized.split("\\?")[0].split("#")[0];
        normalized = normalized.replaceAll("/+", "/");
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    private String extractFilePathFromUrl(String rawUrl) {
        try {
            URI resourceUri = URI.create(rawUrl);
            String resourcePath = resourceUri.getPath();
            String endpoint = normalizeBlank(urlEndpoint);
            if (endpoint == null) {
                return resourcePath;
            }
            URI endpointUri = URI.create(endpoint);
            String endpointPath = endpointUri.getPath();
            if (endpointPath != null && !endpointPath.isBlank() && resourcePath.startsWith(endpointPath)) {
                resourcePath = resourcePath.substring(endpointPath.length());
            }
            return resourcePath;
        } catch (Exception e) {
            return rawUrl;
        }
    }

    private String normalizeFolder(String folder) {
        String normalized = normalizeBlank(folder);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replace('\\', '/').replaceAll("/+", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isBlank() ? null : normalized;
    }

    private String toApiFolder(String folder) {
        String normalized = normalizeFolder(folder);
        return normalized == null ? null : "/" + normalized;
    }

    private String toApiPath(String folder) {
        String normalized = normalizeFolder(folder);
        return normalized == null ? null : "/" + normalized + "/";
    }

    private String extractFolderPath(String filePath) {
        String normalized = normalizeFilePath(filePath);
        if (normalized == null) {
            return null;
        }
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash <= 0) {
            return null;
        }
        return normalizeFolder(normalized.substring(0, lastSlash));
    }

    private String extractFileName(String filePath) {
        String normalized = normalizeFilePath(filePath);
        if (normalized == null) {
            return DEFAULT_UPLOAD_NAME;
        }
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }

    private String ensureExtension(String candidate, String originalFilename) {
        String normalized = normalizeBlank(candidate);
        if (normalized == null) {
            normalized = safeOriginalFilename(originalFilename);
        }
        int lastSlash = normalized.lastIndexOf('/');
        int lastDot = normalized.lastIndexOf('.');
        if (lastDot > lastSlash) {
            return normalized;
        }

        String original = normalizeBlank(originalFilename);
        if (original == null) {
            return normalized;
        }
        int originalDot = original.lastIndexOf('.');
        if (originalDot > original.lastIndexOf('/')) {
            return normalized + original.substring(originalDot);
        }
        return normalized;
    }

    private String safeOriginalFilename(String originalFilename) {
        String normalized = normalizeBlank(originalFilename);
        if (normalized == null) {
            return DEFAULT_UPLOAD_NAME;
        }
        normalized = normalized.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        normalized = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        return normalized.isBlank() ? DEFAULT_UPLOAD_NAME : normalized;
    }

    private boolean looksLikePathOrUrl(String value) {
        return value.contains("/") || value.startsWith("http://") || value.startsWith("https://");
    }

    private String stripExtension(String filePath) {
        String normalized = normalizeFilePath(filePath);
        if (normalized == null) {
            return null;
        }
        int lastSlash = normalized.lastIndexOf('/');
        int lastDot = normalized.lastIndexOf('.');
        if (lastDot > lastSlash) {
            return normalized.substring(0, lastDot);
        }
        return normalized;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String extractErrorMessage(RestClientResponseException exception) {
        String responseBody = normalizeBlank(exception.getResponseBodyAsString());
        if (responseBody == null) {
            return exception.getMessage();
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = normalizeBlank(root.path("message").asText(null));
            if (message != null) {
                return message;
            }
        } catch (Exception ignored) {
            log.debug("ImageKit: khong parse duoc response body loi.");
        }
        return responseBody;
    }

    private record UploadTarget(String folder, String fileName, boolean overwriteExisting) {
    }

    private record StoragePointer(String fileId, String filePath) {
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ImageKitUploadResponse(String fileId, String filePath, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ImageKitFileDetails(String fileId, String filePath) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ImageKitFileSummary(String fileId, String filePath) {
    }
}
