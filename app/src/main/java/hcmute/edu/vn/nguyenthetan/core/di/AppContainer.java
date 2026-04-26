package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomCatalogRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomCommentRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomHomeRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomLeaderboardRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomLessonRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomProfileRepository;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.HealthStatusDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCategorySyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLeaderboardSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLessonSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteNotificationSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteProgressSyncManager;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.SyncSentenceCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.SyncLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;
import retrofit2.Response;

public class AppContainer {

    private static final long POST_LOGIN_REFRESH_DELAY_MS = 1200L;

    private static final String TAG = "AppContainer";
    private static final String DEFAULT_SERVER_ERROR_MESSAGE = "Không thể kết nối đến server. Hãy kiểm tra backend và thử lại.";

    public interface ServerCheckCallback {
        void onResult(boolean available, String message);
    }

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final GetExploreCatalogUseCase getExploreCatalogUseCase;
    private final GetLessonCollectionUseCase getLessonCollectionUseCase;
    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final SyncLessonProgressUseCase syncLessonProgressUseCase;
    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final SyncLeaderboardUseCase syncLeaderboardUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final SyncSentenceCommentsUseCase syncSentenceCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncCategoryCollectionUseCase syncCategoryCollectionUseCase;
    private final hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncSectionLessonsUseCase syncSectionLessonsUseCase;
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(true);
    private final MutableLiveData<String> syncErrorMessage = new MutableLiveData<>();
    private final RemoteCategorySyncManager remoteCategorySyncManager;
    private final RemoteLeaderboardSyncManager remoteLeaderboardSyncManager;
    private final RemoteNotificationSyncManager remoteNotificationSyncManager;
    private final RemoteProgressSyncManager remoteProgressSyncManager;
    private final ExecutorService ioExecutor;
    private final TungTungDatabase database;
    private final MobileApiService mobileApiService;
    private final UserSessionStore userSessionStore;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final Object refreshLock = new Object();
    private boolean refreshAfterSyncPending;
    private boolean delayedRefreshScheduled;

    public AppContainer(Context context) {
        this.appContext = context.getApplicationContext();
        userSessionStore = new UserSessionStore(context);
        database = DatabaseModule.createDatabase(context);

        ioExecutor = Executors.newFixedThreadPool(3);

        mobileApiService = NetworkModule.createMobileApiService(userSessionStore);
        remoteCategorySyncManager = new RemoteCategorySyncManager(mobileApiService, database);
        RemoteLessonSyncManager remoteLessonSyncManager = new RemoteLessonSyncManager(mobileApiService, database);
        remoteLeaderboardSyncManager = new RemoteLeaderboardSyncManager(mobileApiService, database);
        remoteProgressSyncManager = new RemoteProgressSyncManager(context, mobileApiService, database, userSessionStore);
        remoteNotificationSyncManager = new RemoteNotificationSyncManager(context, mobileApiService, database, userSessionStore);

        sync();

        RoomCatalogRepository catalogRepository = new RoomCatalogRepository(
                database.categoryDao(),
                database.sectionDao(),
                database.lessonDao(),
                database.guestProgressDao(),
                remoteCategorySyncManager,
                userSessionStore
        );
        RoomLessonRepository lessonRepository = new RoomLessonRepository(
                database.lessonDao(),
                database.sentenceDao(),
                database.guestProgressDao(),
                database.appSettingsDao(),
                database.sectionDao(),
                database.categoryDao(),
                remoteLessonSyncManager,
                remoteProgressSyncManager,
                userSessionStore
        );
        RoomHomeRepository homeRepository = new RoomHomeRepository(
                database.profileDao(),
                database.streakDayDao(),
                database.lessonDao(),
                database.guestProgressDao(),
                database.recommendationDao(),
                database.appSettingsDao(),
                database.sectionDao(),
                database.categoryDao(),
                userSessionStore
        );
        RoomProfileRepository profileRepository = new RoomProfileRepository(
                database.profileDao(),
                database.dailyActivityDao(),
                database.streakDayDao(),
                userSessionStore
        );
        RoomLeaderboardRepository leaderboardRepository = new RoomLeaderboardRepository(database.leaderboardDao());
        RoomCommentRepository commentRepository = new RoomCommentRepository(database.commentDao());

        getHomeDashboardUseCase = new GetHomeDashboardUseCase(homeRepository);
        getExploreCatalogUseCase = new GetExploreCatalogUseCase(catalogRepository);
        getLessonCollectionUseCase = new GetLessonCollectionUseCase(catalogRepository);
        getLessonSessionUseCase = new GetLessonSessionUseCase(lessonRepository);
        getLessonProgressUseCase = new GetLessonProgressUseCase(lessonRepository);
        syncLessonProgressUseCase = new SyncLessonProgressUseCase(lessonRepository);
        getLeaderboardUseCase = new GetLeaderboardUseCase(leaderboardRepository);
        syncLeaderboardUseCase = new SyncLeaderboardUseCase(remoteLeaderboardSyncManager);
        getProfileUseCase = new GetProfileUseCase(profileRepository);
        getCommentsUseCase = new GetCommentsUseCase(commentRepository);
        syncSentenceCommentsUseCase = new SyncSentenceCommentsUseCase(mobileApiService, database.commentDao());
        checkDictationAnswerUseCase = new CheckDictationAnswerUseCase();
        saveSentenceStatusUseCase = new SaveSentenceStatusUseCase(lessonRepository);
        saveSpeakingAttemptUseCase = new SaveSpeakingAttemptUseCase(lessonRepository);
        syncCategoryCollectionUseCase = new hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncCategoryCollectionUseCase(catalogRepository);
        syncSectionLessonsUseCase = new hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncSectionLessonsUseCase(catalogRepository);
    }

    public void sync() {
        ioExecutor.execute(() -> {
            syncErrorMessage.postValue(null);
            isSyncing.postValue(true);
            try {
                Log.d(TAG, "Starting parallel remote sync.");
                CountDownLatch latch = new CountDownLatch(3);

                ioExecutor.execute(() -> {
                    try { remoteCategorySyncManager.syncCategories(); }
                    catch (Exception e) { Log.w(TAG, "Category sync failed: " + e.getMessage()); }
                    finally { latch.countDown(); }
                });
                ioExecutor.execute(() -> {
                    try { remoteLeaderboardSyncManager.sync(); }
                    catch (Exception e) { Log.w(TAG, "Leaderboard sync failed: " + e.getMessage()); }
                    finally { latch.countDown(); }
                });
                ioExecutor.execute(() -> {
                    try {
                        syncProfile();
                        remoteProgressSyncManager.syncCurrentUserProgress();
                        remoteNotificationSyncManager.flushPendingReminderDeliveries();
                    } catch (Exception e) { Log.w(TAG, "Profile/progress sync failed: " + e.getMessage()); }
                    finally { latch.countDown(); }
                });

                latch.await();
                Log.d(TAG, "Parallel remote sync finished.");
                if (database.categoryDao().count() <= 0) {
                    syncErrorMessage.postValue("Không thể tải dữ liệu từ server, hãy thử kiểm tra lại kết nối!");
                }
            } catch (Exception exception) {
                Log.e(TAG, "Remote sync failed.", exception);
                if (database.categoryDao().count() <= 0) {
                    syncErrorMessage.postValue("Không thể tải dữ liệu từ server, hãy thử kiểm tra lại kết nối!");
                }
            } finally {
                isSyncing.postValue(false);
                runUserRefreshIfPossible();
            }
        });
    }

    public void refreshCurrentUserProfile() {
        synchronized (refreshLock) {
            refreshAfterSyncPending = true;
            if (delayedRefreshScheduled) {
                return;
            }
            delayedRefreshScheduled = true;
        }
        mainHandler.postDelayed(() -> {
            synchronized (refreshLock) {
                delayedRefreshScheduled = false;
            }
            runUserRefreshIfPossible();
        }, POST_LOGIN_REFRESH_DELAY_MS);
    }

    private void runUserRefreshIfPossible() {
        synchronized (refreshLock) {
            if (!refreshAfterSyncPending || Boolean.TRUE.equals(isSyncing.getValue())) {
                return;
            }
            refreshAfterSyncPending = false;
        }
        ioExecutor.execute(() -> {
            syncErrorMessage.postValue(null);
            isSyncing.postValue(true);
            try {
                syncProfile();
                remoteProgressSyncManager.syncCurrentUserProgress();
                remoteNotificationSyncManager.flushPendingReminderDeliveries();
            } finally {
                isSyncing.postValue(false);
            }
        });
    }

    /**
     * Syncs user profile from backend API to local Room DB.
     * This ensures local data (todayStudyMinutes, missedDays, etc.) is available
     * for immediate mascot mood resolution without an extra async API call.
     */
    private void syncProfile() {
        if (!userSessionStore.isLoggedIn()) {
            return;
        }
        long userId = userSessionStore.getUserId();
        if (userId <= 0) {
            return;
        }
        try {
            Response<UserProfileDto> response = RetryUtil.retryWithBackoff(
                    () -> mobileApiService.getProfile(userId).execute(),
                    2,
                    300L,
                    1000L,
                    2.0
            );
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Profile sync skipped. HTTP " + response.code());
                return;
            }
            UserProfileDto dto = response.body();
            int daysSince = resolveDaysSinceLastStudy(dto);
            int todaySeconds = MascotMoodResolver.getTodaySeconds(dto.weeklyActivity);
            int todayMinutes = todaySeconds / 60;
            int weekMinutes = dto.activeTime7d / 60;
            int totalMinutes = dto.totalActiveTime / 60;
            MascotMoodResolver.Mood mood = MascotMoodResolver.resolve(daysSince, todaySeconds);
            hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore.setCurrentMood(appContext, mood);

            ProfileEntity profile = new ProfileEntity(
                    1L,
                    dto.username,
                    dto.email,
                    dto.currentStreak,
                    dto.longestStreak,
                    totalMinutes,
                    todayMinutes,
                    weekMinutes,
                    false,
                    Boolean.TRUE.equals(dto.notificationsEnabled),
                    mood.getId(),
                    mood.getLabel(),
                    "",
                    daysSince,
                    daysSince >= 1
            );
            database.profileDao().upsert(profile);

            // Sync streak days from weeklyActivity
            if (dto.weeklyActivity != null && dto.weeklyActivity.size() == 7) {
                List<StreakDayEntity> streakDays = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    boolean studied = dto.weeklyActivity.get(i) > 0;
                    boolean isToday = (i == 6);
                    streakDays.add(new StreakDayEntity(i, studied, isToday));
                }
                database.streakDayDao().deleteAll();
                database.streakDayDao().insertAll(streakDays);
            }

            // Sync daily activity chart data from weeklyActivity
            if (dto.weeklyActivity != null && dto.weeklyActivity.size() == 7) {
                List<DailyActivityEntity> dailyActivities = new ArrayList<>();
                String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                // weeklyActivity index 6 = today, index 0 = 6 days ago
                for (int i = 0; i < 7; i++) {
                    int daysAgo = 6 - i;
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.add(Calendar.DAY_OF_YEAR, -daysAgo);
                    int dow = dayCal.get(Calendar.DAY_OF_WEEK);
                    // Map Calendar.DAY_OF_WEEK (Sun=1..Sat=7) to label index (Mon=0..Sun=6)
                    int labelIdx = (dow + 5) % 7;
                    int mins = dto.weeklyActivity.get(i) / 60;
                    boolean highlighted = (i == 6);
                    dailyActivities.add(new DailyActivityEntity(i, dayLabels[labelIdx], mins, highlighted));
                }
                database.dailyActivityDao().deleteAll();
                database.dailyActivityDao().insertAll(dailyActivities);
            }

            Log.d(TAG, "Profile synced. todayMin=" + todayMinutes + ", missedDays=" + daysSince + ", mood=" + mood.name());
        } catch (Exception exception) {
            Log.w(TAG, "Profile sync failed: " + exception.getMessage());
        }
    }

    private int resolveDaysSinceLastStudy(UserProfileDto dto) {
        int weeklyDaysSince = MascotMoodResolver.computeDaysSinceLastStudy(dto.weeklyActivity);
        int daysSince = dto.missedDays == null
                ? weeklyDaysSince
                : Math.max(dto.missedDays, weeklyDaysSince);
        if (dto.totalActiveTime > 0 && dto.activeTime30d <= 0) {
            daysSince = Math.max(daysSince, 30);
        }
        return Math.max(daysSince, 0);
    }

    public GetHomeDashboardUseCase getHomeDashboardUseCase() {
        return getHomeDashboardUseCase;
    }

    public GetExploreCatalogUseCase getExploreCatalogUseCase() {
        return getExploreCatalogUseCase;
    }

    public GetLessonCollectionUseCase getLessonCollectionUseCase() {
        return getLessonCollectionUseCase;
    }

    public GetLessonSessionUseCase getLessonSessionUseCase() {
        return getLessonSessionUseCase;
    }

    public GetLessonProgressUseCase getLessonProgressUseCase() {
        return getLessonProgressUseCase;
    }

    public SyncLessonProgressUseCase getSyncLessonProgressUseCase() {
        return syncLessonProgressUseCase;
    }

    public GetLeaderboardUseCase getLeaderboardUseCase() {
        return getLeaderboardUseCase;
    }

    public SyncLeaderboardUseCase getSyncLeaderboardUseCase() {
        return syncLeaderboardUseCase;
    }

    public GetProfileUseCase getProfileUseCase() {
        return getProfileUseCase;
    }

    public GetCommentsUseCase getCommentsUseCase() {
        return getCommentsUseCase;
    }

    public SyncSentenceCommentsUseCase getSyncSentenceCommentsUseCase() {
        return syncSentenceCommentsUseCase;
    }

    public CheckDictationAnswerUseCase getCheckDictationAnswerUseCase() {
        return checkDictationAnswerUseCase;
    }

    public SaveSentenceStatusUseCase getSaveSentenceStatusUseCase() {
        return saveSentenceStatusUseCase;
    }

    public SaveSpeakingAttemptUseCase getSaveSpeakingAttemptUseCase() {
        return saveSpeakingAttemptUseCase;
    }

    public hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncCategoryCollectionUseCase getSyncCategoryCollectionUseCase() {
        return syncCategoryCollectionUseCase;
    }

    public hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncSectionLessonsUseCase getSyncSectionLessonsUseCase() {
        return syncSectionLessonsUseCase;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public MobileApiService getMobileApiService() {
        return mobileApiService;
    }

    public LiveData<String> getSyncErrorMessage() {
        return syncErrorMessage;
    }

    public UserSessionStore getUserSessionStore() {
        return userSessionStore;
    }

    public boolean hasCatalogData() {
        return database.categoryDao().count() > 0;
    }

    public void checkServerAvailability(ServerCheckCallback callback) {
        ioExecutor.execute(() -> {
            boolean available = false;
            String message = DEFAULT_SERVER_ERROR_MESSAGE;
            try {
                Response<HealthStatusDto> healthResponse = RetryUtil.retryWithBackoff(
                        () -> mobileApiService.getHealth().execute(),
                        1,
                        300L,
                        1000L,
                        2.0
                );
                available = healthResponse.isSuccessful()
                        && healthResponse.body() != null
                        && "UP".equalsIgnoreCase(healthResponse.body().status);
                if (!available) {
                    Response<MobileBootstrapDto> fallbackResponse = mobileApiService.getBootstrapLite().execute();
                    available = fallbackResponse.isSuccessful() && fallbackResponse.body() != null;
                }
            } catch (Exception exception) {
                Log.e(TAG, "Server availability check failed.", exception);
                message = DEFAULT_SERVER_ERROR_MESSAGE;
            }
            boolean finalAvailable = available;
            String finalMessage = message;
            mainHandler.post(() -> callback.onResult(finalAvailable, finalMessage));
        });
    }
}
