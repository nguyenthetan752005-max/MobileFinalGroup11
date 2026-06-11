package com.english.learning.service.impl.notification;

import com.english.learning.dto.mobile.MobileNotificationFeedResponse;
import com.english.learning.dto.mobile.MobileNotificationItemResponse;
import com.english.learning.dto.mobile.MobileNotificationSummaryResponse;
import com.english.learning.dto.mobile.MobileReminderDeliveryRequest;
import com.english.learning.entity.Comment;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.entity.UserNotification;
import com.english.learning.enums.NotificationType;
import com.english.learning.repository.UserNotificationRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.notification.MobileNotificationService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MobileNotificationServiceImpl implements MobileNotificationService {

    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_REMINDER_TITLE = "Time to study with TungTung";
    private static final String DEFAULT_REMINDER_BODY = "Keep your streak alive with a short English session today.";
    private static final String DEFAULT_REPLY_BODY = "You have a new reply.";

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MobileNotificationFeedResponse getFeed(Long userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        List<MobileNotificationItemResponse> items = userNotificationRepository
                .findByUser_IdOrderByEventAtDesc(userId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toMobileItem)
                .toList();
        return MobileNotificationFeedResponse.builder()
                .unreadCount(userNotificationRepository.countByUser_IdAndReadAtIsNull(userId))
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MobileNotificationSummaryResponse getSummary(Long userId) {
        return MobileNotificationSummaryResponse.builder()
                .unreadCount(userNotificationRepository.countByUser_IdAndReadAtIsNull(userId))
                .build();
    }

    @Override
    public boolean markAsRead(Long userId, Long notificationId) {
        return userNotificationRepository.findByIdAndUser_Id(notificationId, userId)
                .map(notification -> {
                    if (notification.getReadAt() == null) {
                        notification.setReadAt(LocalDateTime.now());
                        userNotificationRepository.save(notification);
                    }
                    return true;
                })
                .orElse(false);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return userNotificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    public void createReplyNotification(Comment parentComment, Comment replyComment) {
        if (parentComment == null || replyComment == null) {
            return;
        }
        User owner = parentComment.getUser();
        User actor = replyComment.getUser();
        if (owner == null || owner.getId() == null || actor == null || actor.getId() == null) {
            return;
        }
        if (owner.getId().equals(actor.getId())) {
            return;
        }

        String actorName = sanitize(actor.getUsername(), "Someone", 60);
        String title = sanitize(actorName + " replied to your comment", "New reply", 160);
        String body = sanitize(replyComment.getContent(), DEFAULT_REPLY_BODY, 1000);
        String meta = buildReplyMeta(parentComment.getSentence());
        Long lessonId = resolveLessonId(parentComment.getSentence());
        Long sentenceId = parentComment.getSentence() != null ? parentComment.getSentence().getId() : null;
        Long commentId = replyComment.getId();
        LocalDateTime eventAt = replyComment.getCreatedAt() != null ? replyComment.getCreatedAt() : LocalDateTime.now();

        createIfAbsent(
                owner,
                "comment-reply:" + commentId,
                NotificationType.COMMENT_REPLY,
                title,
                body,
                meta,
                eventAt,
                lessonId,
                sentenceId,
                commentId
        );
    }

    @Override
    public void recordDailyReminderDelivery(Long userId, MobileReminderDeliveryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate reminderDate = resolveReminderDate(request);
        LocalDateTime eventAt = resolveReminderEventAt(request);
        String title = sanitize(request == null ? null : request.getTitle(), DEFAULT_REMINDER_TITLE, 160);
        String body = sanitize(request == null ? null : request.getBody(), DEFAULT_REMINDER_BODY, 1000);
        String reminderTime = request == null ? null : request.getReminderTime();
        String reminderTimezone = request == null ? null : request.getReminderTimezone();
        String reminderMeta = request == null
                ? null
                : firstNonBlank(request.getMeta(), buildReminderMeta(reminderTime, reminderTimezone));
        String meta = sanitize(
                reminderMeta,
                "",
                160
        );

        createIfAbsent(
                user,
                "daily-reminder:" + reminderDate,
                NotificationType.DAILY_REMINDER,
                title,
                body,
                meta,
                eventAt,
                request == null ? null : request.getLessonId(),
                null,
                null
        );
    }

    private void createIfAbsent(
            User user,
            String dedupKey,
            NotificationType type,
            String title,
            String body,
            String meta,
            LocalDateTime eventAt,
            Long targetLessonId,
            Long targetSentenceId,
            Long targetCommentId
    ) {
        if (user == null || user.getId() == null || dedupKey == null || dedupKey.isBlank()) {
            return;
        }
        if (userNotificationRepository.findByUser_IdAndDedupKey(user.getId(), dedupKey).isPresent()) {
            return;
        }

        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setDedupKey(dedupKey.trim());
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setMeta(meta == null || meta.isBlank() ? null : meta.trim());
        notification.setEventAt(eventAt == null ? LocalDateTime.now() : eventAt);
        notification.setTargetLessonId(targetLessonId);
        notification.setTargetSentenceId(targetSentenceId);
        notification.setTargetCommentId(targetCommentId);
        userNotificationRepository.save(notification);
    }

    private MobileNotificationItemResponse toMobileItem(UserNotification notification) {
        LocalDateTime eventAt = notification.getEventAt() != null
                ? notification.getEventAt()
                : notification.getCreatedAt();
        return MobileNotificationItemResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .meta(notification.getMeta())
                .timeAgo(TimeFormatUtil.formatTimeAgo(eventAt))
                .read(notification.getReadAt() != null)
                .eventAt(eventAt)
                .targetLessonId(notification.getTargetLessonId())
                .targetSentenceId(notification.getTargetSentenceId())
                .targetCommentId(notification.getTargetCommentId())
                .build();
    }

    private LocalDate resolveReminderDate(MobileReminderDeliveryRequest request) {
        if (request != null && request.getReminderDate() != null && !request.getReminderDate().isBlank()) {
            try {
                return LocalDate.parse(request.getReminderDate().trim());
            } catch (DateTimeParseException ignored) {
                // Fallback below.
            }
        }
        ZoneId zoneId = resolveZoneId(request == null ? null : request.getReminderTimezone());
        Long deliveredAtEpochMillis = request == null ? null : request.getDeliveredAtEpochMillis();
        Instant instant = deliveredAtEpochMillis == null
                ? Instant.now()
                : Instant.ofEpochMilli(deliveredAtEpochMillis);
        return instant.atZone(zoneId).toLocalDate();
    }

    private LocalDateTime resolveReminderEventAt(MobileReminderDeliveryRequest request) {
        ZoneId zoneId = resolveZoneId(request == null ? null : request.getReminderTimezone());
        Long deliveredAtEpochMillis = request == null ? null : request.getDeliveredAtEpochMillis();
        Instant instant = deliveredAtEpochMillis == null
                ? Instant.now()
                : Instant.ofEpochMilli(deliveredAtEpochMillis);
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    private ZoneId resolveZoneId(String rawTimezone) {
        if (rawTimezone == null || rawTimezone.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(rawTimezone.trim());
        } catch (Exception ignored) {
            return ZoneId.systemDefault();
        }
    }

    private String buildReplyMeta(Sentence sentence) {
        if (sentence == null || sentence.getLesson() == null || sentence.getLesson().getTitle() == null) {
            return "";
        }
        return sanitize(sentence.getLesson().getTitle(), "", 160);
    }

    private Long resolveLessonId(Sentence sentence) {
        if (sentence == null || sentence.getLesson() == null) {
            return null;
        }
        return sentence.getLesson().getId();
    }

    private String buildReminderMeta(String reminderTime, String reminderTimezone) {
        String time = sanitize(reminderTime, "", 20);
        String timezone = sanitize(reminderTimezone, "", 60);
        if (time.isEmpty() && timezone.isEmpty()) {
            return "";
        }
        if (timezone.isEmpty()) {
            return time;
        }
        if (time.isEmpty()) {
            return timezone;
        }
        return time + " (" + timezone + ")";
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String sanitize(String value, String fallback, int maxLength) {
        String resolved = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (resolved.isEmpty()) {
            resolved = fallback == null ? "" : fallback.trim();
        }
        if (resolved.length() <= maxLength) {
            return resolved;
        }
        return resolved.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }
}
