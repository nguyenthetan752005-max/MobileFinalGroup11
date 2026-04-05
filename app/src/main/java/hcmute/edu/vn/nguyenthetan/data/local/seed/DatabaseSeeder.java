package hcmute.edu.vn.nguyenthetan.data.local.seed;

import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;

public class DatabaseSeeder {

    private final TungTungDatabase database;

    public DatabaseSeeder(TungTungDatabase database) {
        this.database = database;
    }

    public void seedIfNeeded() {
        if (database.categoryDao().count() > 0) {
            return;
        }

        database.runInTransaction(() -> {
            database.categoryDao().insertAll(CatalogSeedFactory.createCategories());
            database.sectionDao().insertAll(CatalogSeedFactory.createSections());
            database.lessonDao().insertAll(CatalogSeedFactory.createLessons());
            database.sentenceDao().insertAll(CatalogSeedFactory.createSentences());
            database.recommendationDao().insertAll(CatalogSeedFactory.createRecommendations());
            database.profileDao().upsert(UserSeedFactory.createProfile());
            database.appSettingsDao().upsert(UserSeedFactory.createSettings());
            database.dailyActivityDao().insertAll(UserSeedFactory.createDailyActivities());
            database.streakDayDao().insertAll(UserSeedFactory.createStreakDays());
            database.guestProgressDao().insertAll(UserSeedFactory.createGuestProgress());
            database.commentDao().insertAll(CommunitySeedFactory.createComments());
            database.leaderboardDao().insertEntries(CommunitySeedFactory.createLeaderboardEntries());
            database.leaderboardDao().upsertMeta(CommunitySeedFactory.createLeaderboardMeta());
            database.syncStateDao().insertAll(UserSeedFactory.createSyncStates());
        });
    }
}
