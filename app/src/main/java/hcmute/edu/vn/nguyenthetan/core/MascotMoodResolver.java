package hcmute.edu.vn.nguyenthetan.core;

import androidx.annotation.StringRes;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;

import java.util.List;
import java.util.Locale;

/**
 * Determines which mascot mood to display based on study habits.
 *
 * Priority (highest to lowest):
 * 1. Scary — ≥30 days inactive
 * 2. CryingLaughing — ≥5 days inactive
 * 3. Sad — ≥3 days inactive
 * 4. Angry — 1-2 days inactive
 * 5. England — today ≥ 30 min study
 * 6. Stylish — today ≥ 15 min study
 * 7. Origin — default (streak intact)
 */
public class MascotMoodResolver {

    public enum Mood {
        ORIGIN("origin", R.drawable.origin, "Have a good day!"),
        STYLISH("stylish", R.drawable.stylish, "Cool!"),
        ENGLAND("england", R.drawable.england, "Gentleman!"),
        ANGRY("angry", R.drawable.angry, "Get back to study!!!"),
        SAD("sad", R.drawable.sad, "i miss u!"),
        CRYING_LAUGHING("crying_laughing", R.drawable.cryinglaughing, "Pathetic beta lol!"),
        SCARY("scary", R.drawable.scary, "i will find you");

        private final String id;
        private final int drawableRes;
        private final String label;

        Mood(String id, int drawableRes, String label) {
            this.id = id;
            this.drawableRes = drawableRes;
            this.label = label;
        }

        public String getId() {
            return id;
        }

        public int getDrawableRes() {
            return drawableRes;
        }

        public String getLabel() {
            return label;
        }
    }

    public static Mood fromId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return Mood.ORIGIN;
        }
        String normalizedId = rawId.trim().toLowerCase(Locale.US);
        for (Mood mood : Mood.values()) {
            if (mood.id.equals(normalizedId) || mood.name().equalsIgnoreCase(normalizedId)) {
                return mood;
            }
        }
        return Mood.ORIGIN;
    }

    /**
     * Resolves the mascot mood from study data.
     *
     * @param daysSinceLastStudy number of days since the user last studied (0 =
     *                           studied today)
     * @param todayStudySeconds  seconds studied today
     * @return the appropriate Mood
     */
    public static Mood resolve(int daysSinceLastStudy, int todayStudySeconds) {
        // Negative moods first (inactivity)
        if (daysSinceLastStudy >= 30)
            return Mood.SCARY;
        if (daysSinceLastStudy >= 5)
            return Mood.CRYING_LAUGHING;
        if (daysSinceLastStudy >= 3)
            return Mood.SAD;
        if (daysSinceLastStudy >= 1)
            return Mood.ANGRY;

        // Positive moods (today's study time)
        if (todayStudySeconds >= 1800)
            return Mood.ENGLAND; // ≥ 30 min
        if (todayStudySeconds >= 900)
            return Mood.STYLISH; // ≥ 15 min

        return Mood.ORIGIN;
    }

    public static Mood resolve(ProfileEntity profile) {
        if (profile == null) {
            return Mood.ORIGIN;
        }
        if (profile.mascotMoodId != null && !profile.mascotMoodId.trim().isEmpty()) {
            return fromId(profile.mascotMoodId);
        }
        return resolve(
                Math.max(profile.missedDays, 0),
                Math.max(profile.todayStudyMinutes, 0) * 60
        );
    }

    /**
     * Computes daysSinceLastStudy from a weeklyActivity array (7 elements, index 6
     * = today).
     * Counts trailing zeros from the end.
     */
    public static int computeDaysSinceLastStudy(List<Integer> weeklyActivity) {
        if (weeklyActivity == null || weeklyActivity.isEmpty()) {
            return 7; // No data = treat as 7+ days inactive
        }
        for (int i = weeklyActivity.size() - 1; i >= 0; i--) {
            if (weeklyActivity.get(i) > 0) {
                return (weeklyActivity.size() - 1) - i;
            }
        }
        return 7; // All zeros
    }

    /**
     * Gets today's study seconds from weeklyActivity (last element).
     */
    public static int getTodaySeconds(List<Integer> weeklyActivity) {
        if (weeklyActivity == null || weeklyActivity.isEmpty())
            return 0;
        return weeklyActivity.get(weeklyActivity.size() - 1);
    }

    @StringRes
    public static int getReminderTitleRes(Mood mood) {
        if (mood == null) {
            return R.string.daily_reminder_notification_title_default;
        }
        switch (mood) {
            case STYLISH:
                return R.string.daily_reminder_notification_title_stylish;
            case ENGLAND:
                return R.string.daily_reminder_notification_title_england;
            case ANGRY:
                return R.string.daily_reminder_notification_title_angry;
            case SAD:
                return R.string.daily_reminder_notification_title_sad;
            case CRYING_LAUGHING:
                return R.string.daily_reminder_notification_title_crying_laughing;
            case SCARY:
                return R.string.daily_reminder_notification_title_scary;
            case ORIGIN:
            default:
                return R.string.daily_reminder_notification_title_default;
        }
    }

    @StringRes
    public static int getReminderBodyRes(Mood mood) {
        if (mood == null) {
            return R.string.daily_reminder_notification_body_default;
        }
        switch (mood) {
            case STYLISH:
                return R.string.daily_reminder_notification_body_stylish;
            case ENGLAND:
                return R.string.daily_reminder_notification_body_england;
            case ANGRY:
                return R.string.daily_reminder_notification_body_angry;
            case SAD:
                return R.string.daily_reminder_notification_body_sad;
            case CRYING_LAUGHING:
                return R.string.daily_reminder_notification_body_crying_laughing;
            case SCARY:
                return R.string.daily_reminder_notification_body_scary;
            case ORIGIN:
            default:
                return R.string.daily_reminder_notification_body_default;
        }
    }
}
