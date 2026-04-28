package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;

/**
 * Persists a {@link UserProfileDto} fetched from the backend into the local
 * Room tables that drive the home/profile screens. Extracted from
 * {@link AppContainer} so the orchestrator stays focused on wiring.
 */
final class ProfileSyncMapper {

    private static final String[] DAY_LABELS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private final Context appContext;
    private final TungTungDatabase database;

    ProfileSyncMapper(Context appContext, TungTungDatabase database) {
        this.appContext = appContext;
        this.database = database;
    }

    /** Writes profile, streak and weekly-activity rows derived from {@code dto}. */
    void persist(UserProfileDto dto) {
        int daysSince = resolveDaysSinceLastStudy(dto);
        int todaySeconds = MascotMoodResolver.getTodaySeconds(dto.weeklyActivity);
        int todayMinutes = todaySeconds / 60;
        int weekMinutes = dto.activeTime7d / 60;
        int totalMinutes = dto.totalActiveTime / 60;
        MascotMoodResolver.Mood mood = MascotMoodResolver.resolve(daysSince, todaySeconds);
        AppearancePreferenceStore.setCurrentMood(appContext, mood);

        database.profileDao().upsert(new ProfileEntity(
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
        ));

        if (dto.weeklyActivity != null && dto.weeklyActivity.size() == 7) {
            persistStreakDays(dto);
            persistDailyActivities(dto);
        }
    }

    private void persistStreakDays(UserProfileDto dto) {
        List<StreakDayEntity> streakDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            boolean studied = dto.weeklyActivity.get(i) > 0;
            boolean isToday = (i == 6);
            streakDays.add(new StreakDayEntity(i, studied, isToday));
        }
        database.streakDayDao().deleteAll();
        database.streakDayDao().insertAll(streakDays);
    }

    private void persistDailyActivities(UserProfileDto dto) {
        List<DailyActivityEntity> dailyActivities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int daysAgo = 6 - i;
            Calendar dayCal = Calendar.getInstance();
            dayCal.add(Calendar.DAY_OF_YEAR, -daysAgo);
            int dow = dayCal.get(Calendar.DAY_OF_WEEK);
            // Map Calendar.DAY_OF_WEEK (Sun=1..Sat=7) to label index (Mon=0..Sun=6)
            int labelIdx = (dow + 5) % 7;
            int mins = dto.weeklyActivity.get(i) / 60;
            boolean highlighted = (i == 6);
            dailyActivities.add(new DailyActivityEntity(i, DAY_LABELS[labelIdx], mins, highlighted));
        }
        database.dailyActivityDao().deleteAll();
        database.dailyActivityDao().insertAll(dailyActivities);
    }

    private static int resolveDaysSinceLastStudy(UserProfileDto dto) {
        int weeklyDaysSince = MascotMoodResolver.computeDaysSinceLastStudy(dto.weeklyActivity);
        int daysSince = dto.missedDays == null
                ? weeklyDaysSince
                : Math.max(dto.missedDays, weeklyDaysSince);
        if (dto.totalActiveTime > 0 && dto.activeTime30d <= 0) {
            daysSince = Math.max(daysSince, 30);
        }
        return Math.max(daysSince, 0);
    }
}
