package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileNotificationFeedResponse;
import com.english.learning.dto.mobile.MobileNotificationSummaryResponse;
import com.english.learning.dto.mobile.MobileReminderDeliveryRequest;
import com.english.learning.service.notification.MobileNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/notifications")
@RequiredArgsConstructor
public class MobileNotificationController {

    private final MobileNotificationService mobileNotificationService;

    @GetMapping
    public ResponseEntity<MobileNotificationFeedResponse> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "40") int limit
    ) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mobileNotificationService.getFeed(userId, limit));
    }

    @GetMapping("/summary")
    public ResponseEntity<MobileNotificationSummaryResponse> getNotificationSummary(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mobileNotificationService.getSummary(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean updated = mobileNotificationService.markAsRead(userId, notificationId);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "notificationId", notificationId,
                "read", true
        ));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int markedCount = mobileNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "markedCount", markedCount
        ));
    }

    @PostMapping("/reminder-deliveries")
    public ResponseEntity<?> recordReminderDelivery(
            Authentication authentication,
            @RequestBody MobileReminderDeliveryRequest request
    ) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        mobileNotificationService.recordDailyReminderDelivery(userId, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long longValue) {
            return longValue;
        }
        if (principal instanceof Integer intValue) {
            return intValue.longValue();
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
