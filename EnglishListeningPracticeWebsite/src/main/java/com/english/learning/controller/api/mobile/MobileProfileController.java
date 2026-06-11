package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileNotificationPreferenceRequest;
import com.english.learning.dto.mobile.MobileUserProfileResponse;
import com.english.learning.entity.User;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.user.UserService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller: Mobile User Profile API.
 * Provides JSON endpoints for Android profile screen.
 */
@RestController
@RequestMapping("/api/mobile/profile")
@RequiredArgsConstructor
public class MobileProfileController {

    private static final DateTimeFormatter REMINDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final UserService userService;
    private final com.english.learning.service.auth.AuthService authService;
    private final com.english.learning.service.auth.TokenBlacklistService tokenBlacklistService;
    private final AppSettingService appSettingService;
    private final com.english.learning.repository.DailyStudyStatisticRepository dailyStudyStatisticRepository;

    /**
     * GET /api/mobile/profile/{userId}
     * Returns user profile information including streak and weekly activity.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<MobileUserProfileResponse> getProfile(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        String formattedTime = TimeFormatUtil.formatActiveTime(
                user.getTotalActiveTime() != null ? user.getTotalActiveTime() : 0);

        // Compute streak
        List<LocalDate> studyDates = dailyStudyStatisticRepository.findDistinctStudyDatesByUserOrderByDesc(user);
        int[] streaks = computeStreaks(studyDates);
        
        // Compute missed days (days since last study)
        LocalDate today = LocalDate.now();
        int missedDays = 0;
        if (!studyDates.isEmpty()) {
            LocalDate lastStudyDate = studyDates.get(0);
            missedDays = (int) java.time.temporal.ChronoUnit.DAYS.between(lastStudyDate, today);
        } else if (user.getCreatedAt() != null) {
            missedDays = (int) java.time.temporal.ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), today);
        }
        missedDays = Math.max(missedDays, 0);

        // Compute weekly activity (last 7 days)
        LocalDate weekAgo = today.minusDays(6);
        var weeklyStats = dailyStudyStatisticRepository
                .findByUserAndStudyDateBetweenOrderByStudyDateAsc(user, weekAgo, today);
        List<Integer> weeklyActivity = buildWeeklyActivity(weekAgo, today, weeklyStats);

        MobileUserProfileResponse response = MobileUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .totalActiveTime(user.getTotalActiveTime())
                .formattedActiveTime(formattedTime)
                .activeTime7d(user.getActiveTime7d())
                .activeTime30d(user.getActiveTime30d())
                .currentStreak(streaks[0])
                .longestStreak(streaks[1])
                .missedDays(missedDays)
                .weeklyActivity(weeklyActivity)
                .notificationsEnabled(Boolean.TRUE.equals(user.getNotificationsEnabled()))
                .notificationTimezone(user.getNotificationTimezone())
                .dailyReminderEnabled(appSettingService.isDailyReminderEnabled())
                .dailyReminderTime(appSettingService.getDailyReminderTime().format(REMINDER_TIME_FORMATTER))
                .dailyReminderTimezone(appSettingService.getDailyReminderTimezone())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Computes [currentStreak, longestStreak] from a descending list of study dates.
     */
    private int[] computeStreaks(List<LocalDate> studyDates) {
        if (studyDates == null || studyDates.isEmpty()) {
            return new int[]{0, 0};
        }
        int currentStreak = 0;
        int longestStreak = 0;
        int runningStreak = 1;

        // Check if current streak includes today or yesterday
        LocalDate today = LocalDate.now();
        boolean startsFromToday = studyDates.get(0).equals(today) || studyDates.get(0).equals(today.minusDays(1));

        for (int i = 1; i < studyDates.size(); i++) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(studyDates.get(i), studyDates.get(i - 1));
            if (daysBetween == 1) {
                runningStreak++;
            } else {
                if (i == 1 || (currentStreak == 0 && startsFromToday)) {
                    // This was the current streak
                }
                longestStreak = Math.max(longestStreak, runningStreak);
                runningStreak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, runningStreak);

        // Current streak = streak that includes today or yesterday
        if (startsFromToday) {
            runningStreak = 1;
            for (int i = 1; i < studyDates.size(); i++) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(studyDates.get(i), studyDates.get(i - 1));
                if (daysBetween == 1) {
                    runningStreak++;
                } else {
                    break;
                }
            }
            currentStreak = runningStreak;
        }

        return new int[]{currentStreak, longestStreak};
    }

    /**
     * Builds an array of 7 integers (seconds per day) for the last 7 days.
     */
    private List<Integer> buildWeeklyActivity(LocalDate startDate, LocalDate endDate,
                                               List<com.english.learning.entity.DailyStudyStatistic> stats) {
        java.util.Map<LocalDate, Integer> dateMap = new java.util.HashMap<>();
        for (var stat : stats) {
            dateMap.put(stat.getStudyDate(), stat.getActiveTimeSeconds());
        }
        List<Integer> result = new java.util.ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.add(dateMap.getOrDefault(date, 0));
        }
        return result;
    }

    /**
     * PUT /api/mobile/profile/{userId}/username
     * Body: { "newUsername": "..." }
     */
    @PutMapping("/{userId}/username")
    public ResponseEntity<?> updateUsername(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String newUsername = payload.get("newUsername");
        if (newUsername == null || newUsername.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "newUsername required"));
        }

        try {
            userService.updateUsername(userId, newUsername);
            return ResponseEntity.ok(Map.of("success", true, "username", newUsername.trim()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", sanitizeUsernameError(e)));
        }
    }

    /**
     * PUT /api/mobile/profile/{userId}/password
     * Body: { "currentPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "currentPassword and newPassword required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự."));
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        // Verify current password via AuthService
        Optional<User> authenticatedUser = authService.authenticateUser(user.getUsername(), currentPassword);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không chính xác."));
        }

        try {
            authService.updatePassword(user, newPassword);
            // Logout from all devices
            tokenBlacklistService.revokeAllUserTokens(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể đổi mật khẩu lúc này. Vui lòng thử lại."));
        }
    }

    /**
     * PUT /api/mobile/profile/{userId}/notifications
     * Body: { "notificationsEnabled": true, "timezone": "Asia/Bangkok" }
     */
    @PutMapping("/{userId}/notifications")
    public ResponseEntity<?> updateNotificationPreference(
            @PathVariable Long userId,
            @RequestBody MobileNotificationPreferenceRequest request) {
        if (request.getNotificationsEnabled() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "notificationsEnabled required"));
        }

        try {
            userService.updateNotificationPreference(
                    userId,
                    request.getNotificationsEnabled(),
                    request.getTimezone()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "notificationsEnabled", request.getNotificationsEnabled(),
                    "timezone", request.getTimezone()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", sanitizeNotificationError(e)));
        }
    }

    private String sanitizeUsernameError(Exception exception) {
        String message = exception == null || exception.getMessage() == null
                ? ""
                : exception.getMessage().toLowerCase(Locale.US);
        if (message.contains("username")) {
            return "Tên đăng nhập này không khả dụng.";
        }
        return "Không thể cập nhật tên người dùng lúc này.";
    }

    private String sanitizeNotificationError(Exception exception) {
        String message = exception == null || exception.getMessage() == null
                ? ""
                : exception.getMessage().toLowerCase(Locale.US);
        if (message.contains("timezone")) {
            return "Múi giờ không hợp lệ.";
        }
        return "Không thể cập nhật thông báo lúc này.";
    }
}
