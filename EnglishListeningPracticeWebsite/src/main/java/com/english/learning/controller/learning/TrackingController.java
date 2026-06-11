package com.english.learning.controller.learning;

import com.english.learning.entity.User;
import com.english.learning.service.tracking.StudyTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * Design Logic: REST API for frontend time tracking (MVC - Thin Controller).
 *
 * - Receives duration (seconds) from JS after each sentence action.
 * - Anti-cheat cap is enforced in the Service layer, not here.
 * - Uses HttpSession for auth (consistent with rest of the codebase).
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final StudyTrackingService trackingService;

    @PostMapping("/time")
    public ResponseEntity<?> trackTime(@RequestBody Map<String, Integer> payload, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Integer durationSeconds = payload.get("durationSeconds");
        if (durationSeconds != null && durationSeconds > 0) {
            trackingService.addActiveSeconds(user.getUsername(), durationSeconds);
            return ResponseEntity.ok().body(Map.of("message", "Time tracked"));
        }

        return ResponseEntity.badRequest().body("Invalid duration");
    }
}
