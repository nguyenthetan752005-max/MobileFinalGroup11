package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.system.OfflineActionDao;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.GuestSentenceProgressEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.OfflineActionEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileSentenceProgressDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.ProgressUpdateRequestDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.work.OfflineSubmitWorker;
import retrofit2.Call;
import retrofit2.Response;

public class RemoteProgressSyncManager {

    private static final String TAG = "RemoteProgressSync";
    private static final String PREF_NAME = "progress_sync_state";
    private static final String KEY_SCOPED_USER_ID = "scoped_user_id";
    private static final String ACTION_TYPE_PROGRESS = "progress_status";

    private final Context appContext;
    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;
    private final UserSessionStore userSessionStore;
    private final SharedPreferences preferences;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public RemoteProgressSyncManager(
            Context context,
            MobileApiService mobileApiService,
            TungTungDatabase database,
            UserSessionStore userSessionStore
    ) {
        this.appContext = context.getApplicationContext();
        this.mobileApiService = mobileApiService;
        this.database = database;
        this.userSessionStore = userSessionStore;
        this.preferences = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void syncCurrentUserProgressAsync() {
        if (!hasAuthenticatedUser()) {
            return;
        }
        ioExecutor.execute(this::syncCurrentUserProgress);
    }

    public void syncCurrentUserProgress() {
        if (!hasAuthenticatedUser()) {
            return;
        }
        prepareSessionScope();
        syncSnapshotInternal(null);
        if (!flushPendingActionsInternal()) {
            enqueueOfflineFlush();
        }
    }

    public void syncLessonProgress(long lessonId) {
        if (!hasAuthenticatedUser() || lessonId <= 0L) {
            return;
        }
        prepareSessionScope();
        syncSnapshotInternal(lessonId);
    }

    public void submitProgressAsync(long lessonId, long sentenceId, SentenceStatus status) {
        if (!hasAuthenticatedUser() || sentenceId <= 0L || status == null || status == SentenceStatus.NOT_STARTED) {
            return;
        }
        ioExecutor.execute(() -> {
            prepareSessionScope();
            submitProgressInternal(userSessionStore.getUserId(), lessonId, sentenceId, status);
        });
    }

    public boolean flushPendingActions() {
        if (!hasAuthenticatedUser()) {
            database.offlineActionDao().deleteAll();
            return true;
        }
        prepareSessionScope();
        return flushPendingActionsInternal();
    }

    private void submitProgressInternal(long userId, long lessonId, long sentenceId, SentenceStatus status) {
        if (userId <= 0L) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            enqueueOrReplaceAction(userId, lessonId, sentenceId, status);
            enqueueOfflineFlush();
            return;
        }
        boolean pushed = pushStatus(userId, sentenceId, status);
        if (pushed) {
            database.offlineActionDao().deleteBySyncKey(buildSyncKey(userId, sentenceId));
            return;
        }
        if (!hasAuthenticatedUser()) {
            database.offlineActionDao().deleteAll();
            return;
        }
        enqueueOrReplaceAction(userId, lessonId, sentenceId, status);
        enqueueOfflineFlush();
    }

    private boolean flushPendingActionsInternal() {
        if (!hasAuthenticatedUser()) {
            database.offlineActionDao().deleteAll();
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
            if (!ACTION_TYPE_PROGRESS.equals(action.actionType)) {
                continue;
            }
            ProgressActionPayload payload = parsePayload(action.payloadJson);
            if (payload == null || payload.sentenceId <= 0L || payload.userId <= 0L) {
                offlineActionDao.delete(action);
                continue;
            }
            if (payload.userId != currentUserId) {
                offlineActionDao.delete(action);
                continue;
            }
            SentenceStatus status = parseStatus(payload.status);
            if (status == null || status == SentenceStatus.NOT_STARTED) {
                offlineActionDao.delete(action);
                continue;
            }
            if (pushStatus(payload.userId, payload.sentenceId, status)) {
                offlineActionDao.delete(action);
                continue;
            }
            if (!hasAuthenticatedUser()) {
                offlineActionDao.deleteAll();
                return true;
            }
            allSucceeded = false;
            break;
        }
        return allSucceeded;
    }

    private boolean syncSnapshotInternal(Long lessonId) {
        if (!hasAuthenticatedUser() || !NetworkUtils.isNetworkAvailable(appContext)) {
            return false;
        }
        try {
            Response<List<MobileSentenceProgressDto>> response = RetryUtil.retryWithBackoff(
                    () -> mobileApiService.getProgressSnapshot(lessonId).execute(),
                    1,
                    300L,
                    800L,
                    2.0
            );
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Progress snapshot sync skipped. HTTP " + response.code() + ", lessonId=" + lessonId);
                return false;
            }
            replaceLocalCacheFromSnapshot(lessonId, response.body(), userSessionStore.getUserId());
            return true;
        } catch (IOException exception) {
            Log.w(TAG, "Progress snapshot sync failed for lessonId=" + lessonId + ": " + exception.getMessage());
            return false;
        }
    }

    private void replaceLocalCacheFromSnapshot(Long lessonId, List<MobileSentenceProgressDto> snapshot, long userId) {
        GuestProgressDao guestProgressDao = database.guestProgressDao();
        List<GuestSentenceProgressEntity> existingEntries = lessonId == null
                ? guestProgressDao.getAll()
                : guestProgressDao.getByLessonId(lessonId);
        Map<Long, GuestSentenceProgressEntity> existingBySentenceId = new LinkedHashMap<>();
        for (GuestSentenceProgressEntity existing : existingEntries) {
            existingBySentenceId.put(existing.sentenceId, existing);
        }

        Map<Long, MobileSentenceProgressDto> snapshotBySentenceId = new LinkedHashMap<>();
        for (MobileSentenceProgressDto item : snapshot) {
            if (item == null || item.sentenceId <= 0L) {
                continue;
            }
            if (lessonId != null && item.lessonId != lessonId) {
                continue;
            }
            snapshotBySentenceId.put(item.sentenceId, item);
        }

        Map<Long, ProgressActionPayload> pendingBySentenceId = getPendingActionsBySentenceId(userId, lessonId);
        Set<Long> sentenceIds = new LinkedHashSet<>();
        sentenceIds.addAll(snapshotBySentenceId.keySet());
        sentenceIds.addAll(pendingBySentenceId.keySet());

        long now = System.currentTimeMillis();
        List<GuestSentenceProgressEntity> mergedEntries = new ArrayList<>();
        for (Long sentenceId : sentenceIds) {
            MobileSentenceProgressDto snapshotItem = snapshotBySentenceId.get(sentenceId);
            ProgressActionPayload pending = pendingBySentenceId.get(sentenceId);
            GuestSentenceProgressEntity existing = existingBySentenceId.get(sentenceId);

            SentenceStatus mergedStatus = mergeSnapshotStatus(
                    parseStatus(snapshotItem == null ? null : snapshotItem.status),
                    parseStatus(pending == null ? null : pending.status)
            );
            if (mergedStatus == null || mergedStatus == SentenceStatus.NOT_STARTED) {
                continue;
            }

            long resolvedLessonId = snapshotItem != null && snapshotItem.lessonId > 0L
                    ? snapshotItem.lessonId
                    : (pending != null && pending.lessonId > 0L
                    ? pending.lessonId
                    : (existing == null ? 0L : existing.lessonId));
            if (resolvedLessonId <= 0L) {
                continue;
            }

            mergedEntries.add(new GuestSentenceProgressEntity(
                    sentenceId,
                    resolvedLessonId,
                    mergedStatus,
                    existing == null ? null : existing.bestSpeakingScore,
                    existing == null ? null : existing.bestSpeakingTranscript,
                    existing == null ? null : existing.bestSpeakingFeedback,
                    existing == null ? null : existing.bestSpeakingAudioUrl,
                    existing == null ? null : existing.currentSpeakingScore,
                    existing == null ? null : existing.currentSpeakingTranscript,
                    existing == null ? null : existing.currentSpeakingFeedback,
                    existing == null ? null : existing.currentSpeakingAudioUrl,
                    existing == null ? now : Math.max(existing.lastAccessedAt, now)
            ));
        }

        database.runInTransaction(() -> {
            if (lessonId == null) {
                guestProgressDao.deleteAll();
            } else {
                guestProgressDao.deleteByLessonId(lessonId);
            }
            if (!mergedEntries.isEmpty()) {
                guestProgressDao.insertAll(mergedEntries);
            }
        });
    }

    private Map<Long, ProgressActionPayload> getPendingActionsBySentenceId(long userId, Long lessonId) {
        Map<Long, ProgressActionPayload> pendingBySentenceId = new LinkedHashMap<>();
        for (OfflineActionEntity action : database.offlineActionDao().getAllOrdered()) {
            if (!ACTION_TYPE_PROGRESS.equals(action.actionType)) {
                continue;
            }
            ProgressActionPayload payload = parsePayload(action.payloadJson);
            if (payload == null || payload.userId != userId || payload.sentenceId <= 0L) {
                continue;
            }
            if (lessonId != null && payload.lessonId != lessonId) {
                continue;
            }
            pendingBySentenceId.put(payload.sentenceId, payload);
        }
        return pendingBySentenceId;
    }

    private void prepareSessionScope() {
        long currentUserId = userSessionStore.getUserId();
        if (currentUserId <= 0L) {
            return;
        }
        long scopedUserId = preferences.getLong(KEY_SCOPED_USER_ID, -1L);
        if (scopedUserId == currentUserId) {
            return;
        }
        database.runInTransaction(() -> {
            AppSettingsEntity settings = database.appSettingsDao().getSettings();
            if (settings != null) {
                database.appSettingsDao().upsert(new AppSettingsEntity(
                        settings.id,
                        settings.languageCode,
                        settings.onboardingCompleted,
                        null,
                        null,
                        settings.lastViewedCategorySlug
                ));
            }
            database.guestProgressDao().deleteAll();
            database.offlineActionDao().deleteAll();
        });
        preferences.edit().putLong(KEY_SCOPED_USER_ID, currentUserId).apply();
    }

    private void enqueueOrReplaceAction(long userId, long lessonId, long sentenceId, SentenceStatus incomingStatus) {
        OfflineActionDao offlineActionDao = database.offlineActionDao();
        String syncKey = buildSyncKey(userId, sentenceId);
        OfflineActionEntity existingAction = offlineActionDao.getLatestBySyncKey(syncKey);
        SentenceStatus mergedStatus = incomingStatus;
        long resolvedLessonId = lessonId;
        if (existingAction != null) {
            ProgressActionPayload existingPayload = parsePayload(existingAction.payloadJson);
            mergedStatus = mergeQueuedStatus(
                    parseStatus(existingPayload == null ? null : existingPayload.status),
                    incomingStatus
            );
            if (resolvedLessonId <= 0L && existingPayload != null) {
                resolvedLessonId = existingPayload.lessonId;
            }
            offlineActionDao.deleteBySyncKey(syncKey);
        }
        ProgressActionPayload payload = new ProgressActionPayload(
                userId,
                resolvedLessonId,
                sentenceId,
                mergedStatus == null ? null : mergedStatus.name()
        );
        offlineActionDao.insert(new OfflineActionEntity(
                ACTION_TYPE_PROGRESS,
                gson.toJson(payload),
                syncKey,
                System.currentTimeMillis()
        ));
    }

    private boolean pushStatus(long userId, long sentenceId, SentenceStatus status) {
        try {
            ProgressUpdateRequestDto request = new ProgressUpdateRequestDto(userId, sentenceId);
            Call<GenericApiResponseDto> call;
            if (status == SentenceStatus.COMPLETED) {
                call = mobileApiService.completeSentence(request);
            } else if (status == SentenceStatus.SKIPPED) {
                call = mobileApiService.skipSentence(request);
            } else {
                call = mobileApiService.updateProgress(request);
            }

            Response<GenericApiResponseDto> response = call.execute();
            if (!response.isSuccessful()) {
                Log.w(TAG, "Progress push failed. HTTP " + response.code() + " for sentence " + sentenceId);
                return false;
            }
            GenericApiResponseDto body = response.body();
            return body == null || body.success;
        } catch (IOException exception) {
            Log.w(TAG, "Progress push failed for sentence " + sentenceId + ": " + exception.getMessage());
            return false;
        }
    }

    private void enqueueOfflineFlush() {
        if (hasAuthenticatedUser()) {
            OfflineSubmitWorker.enqueue(appContext);
        }
    }

    private boolean hasAuthenticatedUser() {
        return userSessionStore != null && userSessionStore.isLoggedIn();
    }

    private String buildSyncKey(long userId, long sentenceId) {
        return "progress:" + userId + ":" + sentenceId;
    }

    private SentenceStatus mergeQueuedStatus(SentenceStatus existingStatus, SentenceStatus incomingStatus) {
        if (existingStatus == SentenceStatus.COMPLETED || incomingStatus == SentenceStatus.COMPLETED) {
            return SentenceStatus.COMPLETED;
        }
        return incomingStatus == null ? existingStatus : incomingStatus;
    }

    private SentenceStatus mergeSnapshotStatus(SentenceStatus serverStatus, SentenceStatus pendingStatus) {
        if (serverStatus == SentenceStatus.COMPLETED || pendingStatus == SentenceStatus.COMPLETED) {
            return SentenceStatus.COMPLETED;
        }
        if (pendingStatus != null) {
            return pendingStatus;
        }
        return serverStatus;
    }

    private SentenceStatus parseStatus(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        try {
            return SentenceStatus.valueOf(rawValue.trim());
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Unknown sentence status: " + rawValue);
            return null;
        }
    }

    private ProgressActionPayload parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(payloadJson, ProgressActionPayload.class);
        } catch (RuntimeException exception) {
            Log.w(TAG, "Invalid offline progress payload.", exception);
            return null;
        }
    }

    private static class ProgressActionPayload {
        long userId;
        long lessonId;
        long sentenceId;
        String status;

        ProgressActionPayload() {
        }

        ProgressActionPayload(long userId, long lessonId, long sentenceId, String status) {
            this.userId = userId;
            this.lessonId = lessonId;
            this.sentenceId = sentenceId;
            this.status = status;
        }
    }
}
