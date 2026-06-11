package com.english.learning.controller.api.mobile;

import com.english.learning.service.tracking.StudyTrackingService;
import com.english.learning.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller: Mobile Study Time Tracking API.
 * Receives active study time from Android app (stateless, userId in body).
 */
@RestController
@RequestMapping("/api/mobile/tracking")
@RequiredArgsConstructor
public class MobileTrackingController {

    private final StudyTrackingService trackingService;
    private final UserService userService;

    /**
     * POST /api/mobile/tracking/time
     * Body: { "userId": 1, "durationSeconds": 30 }
     */
    @PostMapping("/time")
    public ResponseEntity<?> trackTime(@RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null
                ? Long.valueOf(payload.get("userId").toString()) : null;
        Integer durationSeconds = payload.get("durationSeconds") != null
                ? Integer.valueOf(payload.get("durationSeconds").toString()) : null;

        if (userId == null || durationSeconds == null || durationSeconds <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid userId and durationSeconds required"));
        }

        // Look up username for the tracking service
        return userService.findById(userId)
                .map(user -> {
                    trackingService.addActiveSeconds(user.getUsername(), durationSeconds);
                    return ResponseEntity.ok(Map.of("success", true, "message", "Time tracked"));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "User not found")));
    }
}
