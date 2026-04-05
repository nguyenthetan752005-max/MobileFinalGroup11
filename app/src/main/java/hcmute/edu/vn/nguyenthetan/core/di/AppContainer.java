package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.BuildConfig;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.seed.DatabaseSeeder;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomCatalogRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomCommentRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomHomeRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomLeaderboardRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomLessonRepository;
import hcmute.edu.vn.nguyenthetan.data.repository.RoomProfileRepository;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCatalogSyncManager;
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
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppContainer {

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
    private final ExecutorService ioExecutor;
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(true);

    public AppContainer(Context context) {
        TungTungDatabase database = Room.databaseBuilder(
                        context.getApplicationContext(),
                        TungTungDatabase.class,
                        "tungtung.db"
                )
                .fallbackToDestructiveMigration(false)
                .allowMainThreadQueries()
                .build();

        new DatabaseSeeder(database).seedIfNeeded();
        ioExecutor = Executors.newSingleThreadExecutor();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.TUNGTUNG_API_BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MobileApiService mobileApiService = retrofit.create(MobileApiService.class);
        RemoteCatalogSyncManager remoteCatalogSyncManager = new RemoteCatalogSyncManager(mobileApiService, database);
        ioExecutor.execute(() -> {
            isSyncing.postValue(true);
            try {
                remoteCatalogSyncManager.sync();
            } finally {
                isSyncing.postValue(false);
            }
        });

        RoomCatalogRepository catalogRepository = new RoomCatalogRepository(
                database.categoryDao(),
                database.sectionDao(),
                database.lessonDao(),
                database.guestProgressDao()
        );
        RoomLessonRepository lessonRepository = new RoomLessonRepository(
                database.lessonDao(),
                database.sentenceDao(),
                database.guestProgressDao(),
                database.appSettingsDao(),
                database.sectionDao(),
                database.categoryDao()
        );
        RoomHomeRepository homeRepository = new RoomHomeRepository(
                database.profileDao(),
                database.streakDayDao(),
                database.lessonDao(),
                database.guestProgressDao(),
                database.recommendationDao(),
                database.appSettingsDao()
        );
        RoomProfileRepository profileRepository = new RoomProfileRepository(
                database.profileDao(),
                database.dailyActivityDao(),
                database.streakDayDao()
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
}
