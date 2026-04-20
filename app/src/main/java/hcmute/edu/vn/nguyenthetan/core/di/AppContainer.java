package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCatalogSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCategorySyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLeaderboardSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLessonSyncManager;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;
import retrofit2.Response;

public class AppContainer {

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
    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(true);
    private final MutableLiveData<String> syncErrorMessage = new MutableLiveData<>();
    private final RemoteCatalogSyncManager remoteCatalogSyncManager;
    private final RemoteLeaderboardSyncManager remoteLeaderboardSyncManager;
    private final ExecutorService ioExecutor;
    private final TungTungDatabase database;
    private final MobileApiService mobileApiService;
    private final UserSessionStore userSessionStore;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AppContainer(Context context) {
        userSessionStore = new UserSessionStore(context);
        database = DatabaseModule.createDatabase(context);

        ioExecutor = Executors.newSingleThreadExecutor();

        mobileApiService = NetworkModule.createMobileApiService(userSessionStore);
        RemoteCategorySyncManager remoteCategorySyncManager = new RemoteCategorySyncManager(mobileApiService, database);
        remoteCatalogSyncManager = new RemoteCatalogSyncManager(mobileApiService, database);
        RemoteLessonSyncManager remoteLessonSyncManager = new RemoteLessonSyncManager(mobileApiService, database);
        remoteLeaderboardSyncManager = new RemoteLeaderboardSyncManager(mobileApiService, database);

        sync();

        RoomCatalogRepository catalogRepository = new RoomCatalogRepository(
                database.categoryDao(),
                database.sectionDao(),
                database.lessonDao(),
                database.guestProgressDao(),
                remoteCategorySyncManager
        );
        RoomLessonRepository lessonRepository = new RoomLessonRepository(
                database.lessonDao(),
                database.sentenceDao(),
                database.guestProgressDao(),
                database.appSettingsDao(),
                database.sectionDao(),
                database.categoryDao(),
                remoteLessonSyncManager,
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
        getLeaderboardUseCase = new GetLeaderboardUseCase(leaderboardRepository);
        getProfileUseCase = new GetProfileUseCase(profileRepository);
        getCommentsUseCase = new GetCommentsUseCase(commentRepository);
        checkDictationAnswerUseCase = new CheckDictationAnswerUseCase();
        saveSentenceStatusUseCase = new SaveSentenceStatusUseCase(lessonRepository);
        saveSpeakingAttemptUseCase = new SaveSpeakingAttemptUseCase(lessonRepository);
    }

    public void sync() {
        ioExecutor.execute(() -> {
            syncErrorMessage.postValue(null);
            isSyncing.postValue(true);
            try {
                Log.d(TAG, "Starting remote catalog sync.");
                remoteCatalogSyncManager.sync();
                remoteLeaderboardSyncManager.sync();
                Log.d(TAG, "Remote catalog sync finished.");
                if (database.categoryDao().count() <= 0) {
                    syncErrorMessage.postValue("Không thể tải dữ liệu từ server, hãy thử kiểm tra lại kết nối!");
                }
            } catch (Exception exception) {
                Log.e(TAG, "Remote catalog sync failed.", exception);
                if (database.categoryDao().count() <= 0) {
                    syncErrorMessage.postValue("Không thể tải dữ liệu từ server, hãy thử kiểm tra lại kết nối!");
                }
            } finally {
                isSyncing.postValue(false);
            }
        });
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

    public GetLeaderboardUseCase getLeaderboardUseCase() {
        return getLeaderboardUseCase;
    }

    public GetProfileUseCase getProfileUseCase() {
        return getProfileUseCase;
    }

    public GetCommentsUseCase getCommentsUseCase() {
        return getCommentsUseCase;
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
                        3,
                        500L,
                        2000L,
                        2.0
                );
                available = healthResponse.isSuccessful()
                        && healthResponse.body() != null
                        && "UP".equalsIgnoreCase(healthResponse.body().status);
                if (!available) {
                    Response<MobileBootstrapDto> fallbackResponse = mobileApiService.getBootstrapLite().execute();
                    available = fallbackResponse.isSuccessful() && fallbackResponse.body() != null;
                    if (!available) {
                        message = "Server không phản hồi hợp lệ. HTTP health="
                                + healthResponse.code()
                                + ", bootstrap="
                                + fallbackResponse.code()
                                + ".";
                    }
                }
            } catch (Exception exception) {
                Log.e(TAG, "Server availability check failed.", exception);
                String detail = exception.getMessage();
                if (detail != null && !detail.trim().isEmpty()) {
                    message = DEFAULT_SERVER_ERROR_MESSAGE + "\n" + detail;
                }
            }
            boolean finalAvailable = available;
            String finalMessage = message;
            mainHandler.post(() -> callback.onResult(finalAvailable, finalMessage));
        });
    }
}
