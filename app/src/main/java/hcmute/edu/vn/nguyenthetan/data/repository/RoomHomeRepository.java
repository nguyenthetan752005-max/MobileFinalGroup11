package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.AppSettingsDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.LessonDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.ProfileDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.RecommendationDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.StreakDayDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.RecommendationEntity;
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

    public RoomHomeRepository(
            ProfileDao profileDao,
            StreakDayDao streakDayDao,
            LessonDao lessonDao,
            GuestProgressDao guestProgressDao,
            RecommendationDao recommendationDao,
            AppSettingsDao appSettingsDao
    ) {
        this.profileDao = profileDao;
        this.streakDayDao = streakDayDao;
        this.lessonDao = lessonDao;
        this.guestProgressDao = guestProgressDao;
        this.recommendationDao = recommendationDao;
        this.appSettingsDao = appSettingsDao;
    }

    @Override
    public HomeDashboard getHomeDashboard() {
        ProfileEntity profile = profileDao.getProfile();
        AppSettingsEntity settings = appSettingsDao.getSettings();

        String firstName = profile.name.split(" ")[0];
        LessonEntity currentLessonEntity = resolveCurrentLesson(settings);
        int completedSentences = guestProgressDao.getCompletedCountForLesson(currentLessonEntity.id);
        CurrentLesson currentLesson = new CurrentLesson(
                currentLessonEntity.id,
                currentLessonEntity.title,
                completedSentences,
                currentLessonEntity.totalSentences,
                "Listening",
                "Speaking"
        );

        List<Recommendation> recommendations = new ArrayList<>();
        for (RecommendationEntity entity : recommendationDao.getAllOrdered()) {
            recommendations.add(new Recommendation(entity.title, entity.level, entity.lessonCount, entity.practiceType));
        }

        return new HomeDashboard(
                "Good morning, " + firstName,
                "Keep your streak going.",
                RepositoryFormatters.buildStreakSummary(profile, streakDayDao.getAllOrdered()),
                currentLesson,
                new StudyStats(
                        RepositoryFormatters.formatMinutes(profile.todayStudyMinutes),
                        RepositoryFormatters.formatMinutes(profile.thisWeekStudyMinutes),
                        profile.longestStreak
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
        return lessons.get(0);
    }
}
