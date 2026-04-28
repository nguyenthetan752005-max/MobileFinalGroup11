package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import hcmute.edu.vn.nguyenthetan.data.remote.dto.HealthStatusDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCategorySyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLeaderboardSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLessonSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteNotificationSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteProgressSyncManager;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.ForgotPasswordUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.LoginUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.RegisterUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.AddCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.DeleteCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.SyncSentenceCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.VoteCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.SyncLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationOnlineUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.EvaluateSpeakingUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetSpeakingResultsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SkipDictationUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.TrackLessonTimeUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationSummaryUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkAllNotificationsReadUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkNotificationReadUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.ChangePasswordUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetReminderSettingsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.UpdateUsernameUseCase;
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
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final GoogleAuthUseCase googleAuthUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final UpdateUsernameUseCase updateUsernameUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetReminderSettingsUseCase getReminderSettingsUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final GetNotificationSummaryUseCase getNotificationSummaryUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final CheckDictationOnlineUseCase checkDictationOnlineUseCase;
    private final SkipDictationUseCase skipDictationUseCase;
    private final EvaluateSpeakingUseCase evaluateSpeakingUseCase;
    private final GetSpeakingResultsUseCase getSpeakingResultsUseCase;
    private final TrackLessonTimeUseCase trackLessonTimeUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final VoteCommentUseCase voteCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
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
    private final ProfileSyncMapper profileSyncMapper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final Object refreshLock = new Object();
    private boolean refreshAfterSyncPending;
    private boolean delayedRefreshScheduled;

    public AppContainer(Context context) {
        this.appContext = context.getApplicationContext();
        userSessionStore = new UserSessionStore(context);
        database = DatabaseModule.createDatabase(context);
        profileSyncMapper = new ProfileSyncMapper(appContext, database);

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

        loginUseCase = new LoginUseCase(mobileApiService);
        registerUseCase = new RegisterUseCase(mobileApiService);
        googleAuthUseCase = new GoogleAuthUseCase(mobileApiService);
        forgotPasswordUseCase = new ForgotPasswordUseCase(mobileApiService);
        updateUsernameUseCase = new UpdateUsernameUseCase(mobileApiService);
        changePasswordUseCase = new ChangePasswordUseCase(mobileApiService);
        getReminderSettingsUseCase = new GetReminderSettingsUseCase(mobileApiService);
        getNotificationsUseCase = new GetNotificationsUseCase(mobileApiService);
        getNotificationSummaryUseCase = new GetNotificationSummaryUseCase(mobileApiService);
        markAllNotificationsReadUseCase = new MarkAllNotificationsReadUseCase(mobileApiService);
        markNotificationReadUseCase = new MarkNotificationReadUseCase(mobileApiService);
        checkDictationOnlineUseCase = new CheckDictationOnlineUseCase(mobileApiService);
        skipDictationUseCase = new SkipDictationUseCase(mobileApiService);
        evaluateSpeakingUseCase = new EvaluateSpeakingUseCase(mobileApiService);
        getSpeakingResultsUseCase = new GetSpeakingResultsUseCase(mobileApiService);
        trackLessonTimeUseCase = new TrackLessonTimeUseCase(mobileApiService);
        addCommentUseCase = new AddCommentUseCase(mobileApiService);
        voteCommentUseCase = new VoteCommentUseCase(mobileApiService);
        deleteCommentUseCase = new DeleteCommentUseCase(mobileApiService);
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
     * Fetches the user's profile from the backend and persists it locally so the
     * home/profile screens can render immediately without another async hop.
     * Mapping is delegated to {@link ProfileSyncMapper}.
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
            profileSyncMapper.persist(response.body());
        } catch (Exception exception) {
            Log.w(TAG, "Profile sync failed: " + exception.getMessage());
        }
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

    public LoginUseCase getLoginUseCase() { return loginUseCase; }
    public RegisterUseCase getRegisterUseCase() { return registerUseCase; }
    public GoogleAuthUseCase getGoogleAuthUseCase() { return googleAuthUseCase; }
    public ForgotPasswordUseCase getForgotPasswordUseCase() { return forgotPasswordUseCase; }
    public UpdateUsernameUseCase getUpdateUsernameUseCase() { return updateUsernameUseCase; }
    public ChangePasswordUseCase getChangePasswordUseCase() { return changePasswordUseCase; }
    public GetReminderSettingsUseCase getReminderSettingsUseCase() { return getReminderSettingsUseCase; }
    public GetNotificationsUseCase getNotificationsUseCase() { return getNotificationsUseCase; }
    public GetNotificationSummaryUseCase getNotificationSummaryUseCase() { return getNotificationSummaryUseCase; }
    public MarkAllNotificationsReadUseCase getMarkAllNotificationsReadUseCase() { return markAllNotificationsReadUseCase; }
    public MarkNotificationReadUseCase getMarkNotificationReadUseCase() { return markNotificationReadUseCase; }
    public CheckDictationOnlineUseCase getCheckDictationOnlineUseCase() { return checkDictationOnlineUseCase; }
    public SkipDictationUseCase getSkipDictationUseCase() { return skipDictationUseCase; }
    public EvaluateSpeakingUseCase getEvaluateSpeakingUseCase() { return evaluateSpeakingUseCase; }
    public GetSpeakingResultsUseCase getSpeakingResultsUseCase() { return getSpeakingResultsUseCase; }
    public TrackLessonTimeUseCase getTrackLessonTimeUseCase() { return trackLessonTimeUseCase; }
    public AddCommentUseCase getAddCommentUseCase() { return addCommentUseCase; }
    public VoteCommentUseCase getVoteCommentUseCase() { return voteCommentUseCase; }
    public DeleteCommentUseCase getDeleteCommentUseCase() { return deleteCommentUseCase; }

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
