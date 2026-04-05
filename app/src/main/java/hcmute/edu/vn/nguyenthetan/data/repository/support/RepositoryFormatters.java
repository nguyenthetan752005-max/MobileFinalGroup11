package hcmute.edu.vn.nguyenthetan.data.repository.support;

import java.util.Arrays;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.MoodState;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.model.home.StreakSummary;

public final class RepositoryFormatters {

    private RepositoryFormatters() {
    }

    public static String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + "h";
        }
        return hours + "h " + remainingMinutes + "m";
    }

    public static StreakSummary buildStreakSummary(ProfileEntity profile, List<StreakDayEntity> streakDays) {
        boolean[] weekStatus = new boolean[Math.max(streakDays.size(), 7)];
        for (int index = 0; index < streakDays.size(); index++) {
            weekStatus[index] = streakDays.get(index).studied;
        }

        return new StreakSummary(
                profile.currentStreak,
                profile.missedDays,
                profile.broken,
                profile.activeMood,
                profile.quote,
                Arrays.asList(
                        weekStatus[0],
                        weekStatus[1],
                        weekStatus[2],
                        weekStatus[3],
                        weekStatus[4],
                        weekStatus[5],
                        weekStatus[6]
                ),
                createMoodStates()
        );
    }

    public static SpeakingAttempt defaultAttempt() {
        return new SpeakingAttempt(58, "Practice and tap record again.", "Try once more with clearer pauses.");
    }

    public static SpeakingAttempt defaultBestAttempt() {
        return new SpeakingAttempt(0, "No speaking attempt yet.", "Record a sentence to see your best result.");
    }

    private static List<MoodState> createMoodStates() {
        return Arrays.asList(
                new MoodState("Happy", "primary"),
                new MoodState("Stylish", "warning"),
                new MoodState("Foreigner", "info"),
                new MoodState("Angry", "error"),
                new MoodState("Begging", "muted"),
                new MoodState("Crying Happy", "success")
        );
    }
}
