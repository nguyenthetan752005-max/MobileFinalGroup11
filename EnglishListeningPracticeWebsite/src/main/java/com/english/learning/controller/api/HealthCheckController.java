package com.english.learning.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health Check Endpoint cho Android App.
 * Dùng để kiểm tra server availability trước khi gọi các API chính.
 */
@RestController
@RequestMapping("/api")
public class HealthCheckController {

    /**
     * GET /api/health
     * 
     * Returns server status cho Android kiểm tra kết nối.
     * Không yêu cầu authentication.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "english-learning-api",
            "timestamp", Instant.now().toString(),
            "version", "1.0.0"
        ));
    }
}
