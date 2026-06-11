package com.english.learning.controller.api.mobile;

import com.english.learning.entity.Sentence;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.util.AudioUrlResolver;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * REST Controller: Mobile Media Proxy API.
 * 
 * Đóng vai trò Proxy tải file Media từ các nguồn lưu trữ bên thứ 3 (như ImageKit)
 * sau đó stream dữ liệu trực tiếp về cho Android App tải hoặc phát (on-the-fly),
 * nhằm né các rào cản 401 Unauthorized do bên thứ 3 đánh vào thiết bị Mobile (ExoPlayer).
 */
@RestController
@RequestMapping("/api/mobile/media")
@RequiredArgsConstructor
@Slf4j
public class MobileMediaProxyController {

    private final SentenceRepository sentenceRepository;
    private final AudioUrlResolver audioUrlResolver;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Bắn luồng Byte trực tiếp của Audio cho Android Client tải về hoặc phát online
     */
    @GetMapping("/sentences/{id}/audio")
    public void streamSentenceAudio(@PathVariable Long id, HttpServletResponse response) {
        Sentence sentence = sentenceRepository.findById(id).orElse(null);

        String audioUrl = sentence != null ? audioUrlResolver.resolve(sentence) : null;
        if (sentence == null || audioUrl == null || audioUrl.isBlank()) {
            log.warn("Media Proxy: Không tìm thấy Audio cho Sentence {}", id);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        try {
            // Thay mặt Android gọi Request lấy Audio file
            restTemplate.execute(audioUrl, HttpMethod.GET, null, clientHttpResponse -> {
                // Set Header trả về Client
                response.setStatus(clientHttpResponse.getStatusCode().value());
                response.setContentType("audio/mpeg");
                response.setHeader("Content-Disposition", "inline; filename=\"audio_" + id + ".mp3\"");
                response.setHeader("Cache-Control", "public, max-age=31536000"); // Cache 1 năm cho Mobile đỡ tải lại

                // Cấp Stream luồng byte thẳng từ mảng trả về
                StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
                return null;
            });
            log.info("Media Proxy: Đã stream thành công Audio cho Sentence {}", id);

        } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
            log.error("Media Proxy: Lỗi từ Media Server khi tải Sentence {} - Status: {} - Body: {} - Headers: {}", 
                id, e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders());
            response.setStatus(e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Media Proxy: Lỗi hệ thống khi tải luồng Stream cho Sentence {}", id, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
