package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.AppSettingsDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.LessonDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.CategoryDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.ProfileDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.RecommendationDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.SectionDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.StreakDayDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.RecommendationEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.repository.support.RepositoryFormatters;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.CurrentLesson;
import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.domain.model.home.Recommendation;
import hcmute.edu.vn.nguyenthetan.domain.model.home.StudyStats;
import hcmute.edu.vn.nguyenthetan.domain.repository.HomeRepository;

public class RoomHomeRepository implements HomeRepository {

    private final ProfileDao profileDao;
    private final StreakDayDao streakDayDao;
    private final LessonDao lessonDao;
    private final GuestProgressDao guestProgressDao;
    private final RecommendationDao recommendationDao;
    private final AppSettingsDao appSettingsDao;
    private final SectionDao sectionDao;
    private final CategoryDao categoryDao;
    private final UserSessionStore userSessionStore;

    public RoomHomeRepository(
            ProfileDao profileDao,
            StreakDayDao streakDayDao,
            LessonDao lessonDao,
            GuestProgressDao guestProgressDao,
            RecommendationDao recommendationDao,
            AppSettingsDao appSettingsDao,
            SectionDao sectionDao,
            CategoryDao categoryDao,
            UserSessionStore userSessionStore
    ) {
        this.profileDao = profileDao;
        this.streakDayDao = streakDayDao;
        this.lessonDao = lessonDao;
        this.guestProgressDao = guestProgressDao;
        this.recommendationDao = recommendationDao;
        this.appSettingsDao = appSettingsDao;
        this.sectionDao = sectionDao;
        this.categoryDao = categoryDao;
        this.userSessionStore = userSessionStore;
    }

    @Override
    public HomeDashboard getHomeDashboard() {
        ProfileEntity profile = profileDao.getProfile();
        AppSettingsEntity settings = appSettingsDao.getSettings();
        if (profile == null) {
            profile = new ProfileEntity(
                    1L,
                    "Guest",
                    "guest@local",
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    true,
                    MascotMoodResolver.Mood.ORIGIN.getId(),
                    "Calm",
                    "",
                    0,
                    false
            );
        }

        boolean guestMode = userSessionStore == null || !userSessionStore.isLoggedIn();
        String firstName = guestMode
                ? "Guest"
                : (profile.name == null || profile.name.trim().isEmpty()
                ? "Guest"
                : profile.name.split(" ")[0]);
        LessonEntity currentLessonEntity = resolveCurrentLesson(settings);
        CurrentLesson currentLesson = null;
        if (currentLessonEntity != null) {
            CategoryEntity currentCategory = resolveCategoryForLesson(currentLessonEntity);
            int completedSentences = guestMode ? 0 : guestProgressDao.getCompletedCountForLesson(currentLessonEntity.id);
            currentLesson = new CurrentLesson(
                    currentLessonEntity.id,
                    currentLessonEntity.title,
                    completedSentences,
                    currentLessonEntity.totalSentences,
                    RepositoryFormatters.formatPracticeType(currentCategory == null ? null : currentCategory.practiceType),
                    RepositoryFormatters.formatContentType(currentLessonEntity.contentType)
            );
        }

        List<Recommendation> recommendations = new ArrayList<>();
        for (RecommendationEntity entity : recommendationDao.getAllOrdered()) {
            recommendations.add(new Recommendation(
                    entity.title,
                    entity.level,
                    entity.lessonCount,
                    RepositoryFormatters.formatPracticeType(entity.practiceType)
            ));
        }

        return new HomeDashboard(
                guestMode ? "Welcome, Guest" : "Good morning, " + firstName,
                guestMode ? "Listen freely now. Create an account to save progress and join the community."
                        : "",
                RepositoryFormatters.buildStreakSummary(guestMode ? buildGuestProfile() : profile, streakDayDao.getAllOrdered()),
                currentLesson,
                new StudyStats(
                        RepositoryFormatters.formatMinutes(guestMode ? 0 : profile.todayStudyMinutes),
                        RepositoryFormatters.formatMinutes(guestMode ? 0 : profile.thisWeekStudyMinutes),
                        guestMode ? 0 : profile.longestStreak
                ),
                recommendations
        );
    }

    private LessonEntity resolveCurrentLesson(AppSettingsEntity settings) {
        if (settings != null && settings.lastOpenedLessonId != null) {
            LessonEntity lesson = lessonDao.getById(settings.lastOpenedLessonId);
            if (lesson != null) {
                return lesson;
            }
        }

        LessonEntity fallback = lessonDao.getById(AppDefaults.DEFAULT_LESSON_ID);
        if (fallback != null) {
            return fallback;
        }

        List<LessonEntity> lessons = lessonDao.getAllOrdered();
        if (!lessons.isEmpty()) {
            return lessons.get(0);
        }

        // No lesson found, return null instead of a dummy lesson
        return null;
    }

    private CategoryEntity resolveCategoryForLesson(LessonEntity lesson) {
        if (lesson == null) {
            return null;
        }
        SectionEntity section = null;
        for (CategoryEntity category : categoryDao.getAllOrdered()) {
            for (SectionEntity item : sectionDao.getByCategoryId(category.id)) {
                if (item.id == lesson.sectionId) {
                    section = item;
                    break;
                }
            }
            if (section != null) {
                break;
            }
        }
        if (section == null) {
            return null;
        }
        for (CategoryEntity category : categoryDao.getAllOrdered()) {
            if (category.id == section.categoryId) {
                return category;
            }
        }
        return null;
    }

    private ProfileEntity buildGuestProfile() {
        return new ProfileEntity(
                1L,
                "Guest",
                "guest@local",
                0,
                0,
                0,
                0,
                0,
                false,
                true,
                MascotMoodResolver.Mood.ORIGIN.getId(),
                "Guest mode",
                "",
                0,
                false
        );
    }
}
