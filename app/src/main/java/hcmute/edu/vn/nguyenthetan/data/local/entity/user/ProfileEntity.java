package hcmute.edu.vn.nguyenthetan.data.local.entity.user;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_local")
public class ProfileEntity {

    @PrimaryKey
    public long id;

    public String name;
    public String email;
    public int currentStreak;
    public int longestStreak;
    public int totalStudyMinutes;
    public int todayStudyMinutes;
    public int thisWeekStudyMinutes;
    public boolean darkModeEnabled;
    public boolean notificationsEnabled;
    public String mascotMoodId;
    public String activeMood;
    public String quote;
    public String avatarUrl;
    public int missedDays;
    public boolean broken;
    public String provider;

    public ProfileEntity(
            long id,
            String name,
            String email,
            int currentStreak,
            int longestStreak,
            int totalStudyMinutes,
            int todayStudyMinutes,
            int thisWeekStudyMinutes,
            boolean darkModeEnabled,
            boolean notificationsEnabled,
            String mascotMoodId,
            String activeMood,
            String quote,
            String avatarUrl,
            int missedDays,
            boolean broken,
            String provider
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.totalStudyMinutes = totalStudyMinutes;
        this.todayStudyMinutes = todayStudyMinutes;
        this.thisWeekStudyMinutes = thisWeekStudyMinutes;
        this.darkModeEnabled = darkModeEnabled;
        this.notificationsEnabled = notificationsEnabled;
        this.mascotMoodId = mascotMoodId;
        this.activeMood = activeMood;
        this.quote = quote;
        this.avatarUrl = avatarUrl;
        this.missedDays = missedDays;
        this.broken = broken;
        this.provider = provider;
    }
}
