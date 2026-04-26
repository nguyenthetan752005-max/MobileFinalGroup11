package hcmute.edu.vn.nguyenthetan.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import hcmute.edu.vn.nguyenthetan.data.local.dao.system.OfflineActionDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.OfflineActionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.AppSettingsDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.CategoryDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.community.CommentDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.DailyActivityDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.community.LeaderboardDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.LessonDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.ProfileDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.RecommendationDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.SectionDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.SentenceDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.StreakDayDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.system.SyncStateDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.GuestSentenceProgressEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.RecommendationEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.SyncStateEntity;

@Database(
        entities = {
                AppSettingsEntity.class,
                CategoryEntity.class,
                CommentEntity.class,
                DailyActivityEntity.class,
                GuestSentenceProgressEntity.class,
                LeaderboardEntryEntity.class,
                LeaderboardMetaEntity.class,
                LessonEntity.class,
                ProfileEntity.class,
                RecommendationEntity.class,
                SectionEntity.class,
                SentenceEntity.class,
                StreakDayEntity.class,
                SyncStateEntity.class,
                OfflineActionEntity.class
        },
        version = 9,
        exportSchema = false
)
@TypeConverters(RoomConverters.class)
public abstract class TungTungDatabase extends RoomDatabase {

    public abstract OfflineActionDao offlineActionDao();

    public abstract AppSettingsDao appSettingsDao();

    public abstract CategoryDao categoryDao();

    public abstract CommentDao commentDao();

    public abstract DailyActivityDao dailyActivityDao();

    public abstract GuestProgressDao guestProgressDao();

    public abstract LeaderboardDao leaderboardDao();

    public abstract LessonDao lessonDao();

    public abstract ProfileDao profileDao();

    public abstract RecommendationDao recommendationDao();

    public abstract SectionDao sectionDao();

    public abstract SentenceDao sentenceDao();

    public abstract StreakDayDao streakDayDao();

    public abstract SyncStateDao syncStateDao();
}
