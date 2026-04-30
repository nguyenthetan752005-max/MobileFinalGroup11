package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.dao.system.OfflineActionDao;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.OfflineActionEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileReminderDeliveryRequestDto;
import hcmute.edu.vn.nguyenthetan.work.OfflineSubmitWorker;
import retrofit2.Response;

public class RemoteNotificationSyncManager {

    private static final String TAG = "RemoteNotificationSync";
    private static final String ACTION_TYPE_REMINDER_DELIVERY = "notification_reminder_delivery";

    private final Context appContext;
    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;
    private final UserSessionStore userSessionStore;
    private final Gson gson = new Gson();

    public RemoteNotificationSyncManager(
            Context context,
            MobileApiService mobileApiService,
            TungTungDatabase database,
            UserSessionStore userSessionStore
    ) {
        this.appContext = context.getApplicationContext();
        this.mobileApiService = mobileApiService;
        this.database = database;
        this.userSessionStore = userSessionStore;
    }

    public void recordReminderDelivery(
            String title,
            String body,
            String reminderTime,
            String reminderTimezone,
            Long lessonId,
            String lessonTitle,
            long deliveredAtEpochMillis
    ) {
        if (!hasAuthenticatedUser()) {
            return;
        }
        ReminderDeliveryPayload payload = new ReminderDeliveryPayload(
                userSessionStore.getUserId(),
                title,
                body,
                buildMeta(reminderTime, reminderTimezone),
                resolveReminderDate(deliveredAtEpochMillis, reminderTimezone),
                reminderTime,
                reminderTimezone,
                deliveredAtEpochMillis,
                lessonId,
                lessonTitle
        );
        if (pushReminderDelivery(payload)) {
            database.offlineActionDao().deleteBySyncKey(buildSyncKey(payload.userId, payload.reminderDate));
            return;
        }
        enqueueOrReplaceAction(payload);
        OfflineSubmitWorker.enqueue(appContext);
    }

    public boolean flushPendingReminderDeliveries() {
        if (!hasAuthenticatedUser()) {
            database.offlineActionDao().deleteByActionType(ACTION_TYPE_REMINDER_DELIVERY);
            return true;
        }
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            return false;
        }

        OfflineActionDao offlineActionDao = database.offlineActionDao();
        List<OfflineActionEntity> actions = offlineActionDao.getAllOrdered();
        long currentUserId = userSessionStore.getUserId();
        boolean allSucceeded = true;
        for (OfflineActionEntity action : actions) {
            if (!ACTION_TYPE_REMINDER_DELIVERY.equals(action.actionType)) {
                continue;
            }
            ReminderDeliveryPayload payload = parsePayload(action.payloadJson);
            if (payload == null || payload.userId <= 0L || payload.reminderDate == null || payload.reminderDate.trim().isEmpty()) {
                offlineActionDao.delete(action);
                continue;
            }
            if (payload.userId != currentUserId) {
                offlineActionDao.delete(action);
                continue;
            }
            if (pushReminderDelivery(payload)) {
                offlineActionDao.delete(action);
                continue;
            }
            allSucceeded = false;
            break;
        }
        return allSucceeded;
    }

    private void enqueueOrReplaceAction(ReminderDeliveryPayload payload) {
        if (payload == null || payload.userId <= 0L || payload.reminderDate == null || payload.reminderDate.trim().isEmpty()) {
            return;
        }
        OfflineActionDao offlineActionDao = database.offlineActionDao();
        String syncKey = buildSyncKey(payload.userId, payload.reminderDate);
        offlineActionDao.deleteBySyncKey(syncKey);
        offlineActionDao.insert(new OfflineActionEntity(
                ACTION_TYPE_REMINDER_DELIVERY,
                gson.toJson(payload),
                syncKey,
                payload.deliveredAtEpochMillis
        ));
    }

    private boolean pushReminderDelivery(ReminderDeliveryPayload payload) {
        try {
            MobileReminderDeliveryRequestDto request = new MobileReminderDeliveryRequestDto(
                    payload.title,
                    payload.body,
                    payload.meta,
                    payload.reminderDate,
                    payload.reminderTime,
                    payload.reminderTimezone,
                    payload.deliveredAtEpochMillis,
                    payload.lessonId,
                    payload.lessonTitle
            );
            Response<GenericApiResponseDto> response = mobileApiService.recordReminderDelivery(request).execute();
            if (!response.isSuccessful()) {
                Log.w(TAG, "Reminder delivery push failed. HTTP " + response.code());
                return false;
            }
            GenericApiResponseDto body = response.body();
            return body == null || body.success;
        } catch (IOException exception) {
            Log.w(TAG, "Reminder delivery push failed: " + exception.getMessage());
            return false;
        }
    }

    private ReminderDeliveryPayload parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(payloadJson, ReminderDeliveryPayload.class);
        } catch (RuntimeException exception) {
            Log.w(TAG, "Invalid offline reminder payload.", exception);
            return null;
        }
    }

    private String resolveReminderDate(long deliveredAtEpochMillis, String rawTimezone) {
        TimeZone timeZone = resolveTimeZone(rawTimezone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setTimeZone(timeZone);
        return formatter.format(new Date(deliveredAtEpochMillis));
    }

    private TimeZone resolveTimeZone(String rawTimezone) {
        if (rawTimezone == null || rawTimezone.trim().isEmpty()) {
            return TimeZone.getDefault();
        }
        return TimeZone.getTimeZone(rawTimezone.trim());
    }

    private String buildMeta(String reminderTime, String reminderTimezone) {
        String safeTime = sanitize(reminderTime);
        String safeTimezone = sanitize(reminderTimezone);
        if (safeTime.isEmpty() && safeTimezone.isEmpty()) {
            return "";
        }
        if (safeTimezone.isEmpty()) {
            return safeTime;
        }
        if (safeTime.isEmpty()) {
            return safeTimezone;
        }
        return safeTime + " (" + safeTimezone + ")";
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildSyncKey(long userId, String reminderDate) {
        return "notification-reminder:" + userId + ":" + reminderDate;
    }

    private boolean hasAuthenticatedUser() {
        return userSessionStore != null && userSessionStore.isLoggedIn();
    }

    private static class ReminderDeliveryPayload {
        long userId;
        String title;
        String body;
        String meta;
        String reminderDate;
        String reminderTime;
        String reminderTimezone;
        long deliveredAtEpochMillis;
        Long lessonId;
        String lessonTitle;

        ReminderDeliveryPayload() {
        }

        ReminderDeliveryPayload(
                long userId,
                String title,
                String body,
                String meta,
                String reminderDate,
                String reminderTime,
                String reminderTimezone,
                long deliveredAtEpochMillis,
                Long lessonId,
                String lessonTitle
        ) {
            this.userId = userId;
            this.title = title;
            this.body = body;
            this.meta = meta;
            this.reminderDate = reminderDate;
            this.reminderTime = reminderTime;
            this.reminderTimezone = reminderTimezone;
            this.deliveredAtEpochMillis = deliveredAtEpochMillis;
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
        }
    }
}
