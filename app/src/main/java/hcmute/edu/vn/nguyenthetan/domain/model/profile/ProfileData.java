package hcmute.edu.vn.nguyenthetan.domain.model.profile;

import java.util.Collections;
import java.util.List;
import hcmute.edu.vn.nguyenthetan.domain.model.home.StreakSummary;
import hcmute.edu.vn.nguyenthetan.domain.model.home.DailyActivity;


public class ProfileData {

    private final String name;
    private final String email;
    private final int currentStreak;
    private final int longestStreak;
    private final String totalStudyTime;
    private final List<DailyActivity> dailyActivities;
    private final StreakSummary streakSummary;
    private final boolean darkModeEnabled;
    private final boolean notificationsEnabled;

    public ProfileData(
            String name,
            String email,
            int currentStreak,
            int longestStreak,
            String totalStudyTime,
            List<DailyActivity> dailyActivities,
            StreakSummary streakSummary,
            boolean darkModeEnabled,
            boolean notificationsEnabled
    ) {
        this.name = name;
        this.email = email;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.totalStudyTime = totalStudyTime;
        this.dailyActivities = Collections.unmodifiableList(dailyActivities);
        this.streakSummary = streakSummary;
        this.darkModeEnabled = darkModeEnabled;
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public String getTotalStudyTime() {
        return totalStudyTime;
    }

    public List<DailyActivity> getDailyActivities() {
        return dailyActivities;
    }

    public StreakSummary getStreakSummary() {
        return streakSummary;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}
