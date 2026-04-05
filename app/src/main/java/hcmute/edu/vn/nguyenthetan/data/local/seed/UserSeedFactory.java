package hcmute.edu.vn.nguyenthetan.data.local.seed;

import java.util.Arrays;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.GuestSentenceProgressEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.SyncStateEntity;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;

final class UserSeedFactory {

    private UserSeedFactory() {
    }

    static ProfileEntity createProfile() {
        return new ProfileEntity(
                1L,
                "Tan Nguyen",
                "tan.nguyen23110152@gmail.com",
                12,
                18,
                2700,
                15,
                150,
                false,
                true,
                "Happy",
                "Small sessions win because they happen every day.",
                0,
                false
        );
    }

    static AppSettingsEntity createSettings() {
        return new AppSettingsEntity(1L, "en", false, AppDefaults.DEFAULT_LESSON_ID, AppDefaults.DEFAULT_SENTENCE_ID, AppDefaults.DEFAULT_CATEGORY_SLUG);
    }

    static List<DailyActivityEntity> createDailyActivities() {
        return Arrays.asList(
                new DailyActivityEntity(1, "Mon", 14, false),
                new DailyActivityEntity(2, "Tue", 19, false),
                new DailyActivityEntity(3, "Wed", 26, true),
                new DailyActivityEntity(4, "Thu", 15, false),
                new DailyActivityEntity(5, "Fri", 8, false),
                new DailyActivityEntity(6, "Sat", 32, false),
                new DailyActivityEntity(7, "Sun", 18, false)
        );
    }

    static List<StreakDayEntity> createStreakDays() {
        return Arrays.asList(
                new StreakDayEntity(1, true, false),
                new StreakDayEntity(2, true, false),
                new StreakDayEntity(3, true, false),
                new StreakDayEntity(4, true, false),
                new StreakDayEntity(5, true, false),
                new StreakDayEntity(6, false, false),
                new StreakDayEntity(7, true, true)
        );
    }

    static List<GuestSentenceProgressEntity> createGuestProgress() {
        long now = System.currentTimeMillis();
        return Arrays.asList(
                new GuestSentenceProgressEntity(5101L, 1001L, SentenceStatus.COMPLETED, 80, "Nice to meet you, my name is Anna", "Clear rhythm.", 80, "Nice to meet you, my name is Anna", "Clear rhythm.", now - 120000L),
                new GuestSentenceProgressEntity(5102L, 1001L, SentenceStatus.COMPLETED, 82, "I am from Ho Chi Minh City", "Good vowels.", 82, "I am from Ho Chi Minh City", "Good vowels.", now - 110000L),
                new GuestSentenceProgressEntity(5103L, 1001L, SentenceStatus.COMPLETED, 84, "Do you study English every day", "Confident pacing.", 84, "Do you study English every day", "Confident pacing.", now - 100000L),
                new GuestSentenceProgressEntity(5104L, 1001L, SentenceStatus.COMPLETED, 83, "I usually listen to short stories", "Natural stress.", 83, "I usually listen to short stories", "Natural stress.", now - 90000L),
                new GuestSentenceProgressEntity(5105L, 1001L, SentenceStatus.COMPLETED, 85, "Speaking slowly helps me hear each sound", "Strong endings.", 85, "Speaking slowly helps me hear each sound", "Strong endings.", now - 80000L),
                new GuestSentenceProgressEntity(5106L, 1001L, SentenceStatus.COMPLETED, 86, "See you again tomorrow morning", "Comfortable delivery.", 86, "See you again tomorrow morning", "Comfortable delivery.", now - 70000L),
                new GuestSentenceProgressEntity(5201L, 1002L, SentenceStatus.COMPLETED, 78, "Our classroom is on the third floor", "Clear enough.", 78, "Our classroom is on the third floor", "Clear enough.", now - 60000L),
                new GuestSentenceProgressEntity(5202L, 1002L, SentenceStatus.COMPLETED, 80, "The history test starts after lunch", "Good control.", 80, "The history test starts after lunch", "Good control.", now - 50000L),
                new GuestSentenceProgressEntity(5203L, 1002L, SentenceStatus.COMPLETED, 77, "Can you help me with this homework", "Keep final sounds sharper.", 77, "Can you help me with this homework", "Keep final sounds sharper.", now - 40000L),
                new GuestSentenceProgressEntity(5204L, 1002L, SentenceStatus.IN_PROGRESS, 68, "We have football practice this afternoon", "You are close. Slow down on afternoon.", 68, "We have football practice this afternoon", "You are close. Slow down on afternoon.", now - 30000L),
                new GuestSentenceProgressEntity(5401L, 1004L, SentenceStatus.COMPLETED, 72, "Can I get a hot latte please", "Passed with steady pace.", 72, "Can I get a hot latte please", "Passed with steady pace.", now - 20000L),
                new GuestSentenceProgressEntity(5402L, 1004L, SentenceStatus.COMPLETED, 71, "Would you like sugar with that", "Good enough.", 71, "Would you like sugar with that", "Good enough.", now - 19000L),
                new GuestSentenceProgressEntity(5403L, 1004L, SentenceStatus.IN_PROGRESS, 65, "Please make it less sweet for me", "Focus on sweet.", 65, "Please make it less sweet for me", "Focus on sweet.", now - 18000L),
                new GuestSentenceProgressEntity(5001L, AppDefaults.DEFAULT_LESSON_ID, SentenceStatus.COMPLETED, 83, "Good morning, how are you today", "Natural tone and pacing.", 83, "Good morning, how are you today", "Natural tone and pacing.", now - 15000L),
                new GuestSentenceProgressEntity(5002L, AppDefaults.DEFAULT_LESSON_ID, SentenceStatus.COMPLETED, 85, "I like listening to Michael Jackson songs", "Strong rhythm and clear word endings. Keep the same pace.", 85, "I like listening to Michael Jackson songs", "Strong rhythm and clear word endings. Keep the same pace.", now - 10000L),
                new GuestSentenceProgressEntity(5003L, AppDefaults.DEFAULT_LESSON_ID, SentenceStatus.IN_PROGRESS, 62, "I like listen to Michael Jackson song", "Stress the ending sounds and slow down slightly.", 62, "I like listen to Michael Jackson song", "Stress the ending sounds and slow down slightly.", now - 5000L)
        );
    }

    static List<SyncStateEntity> createSyncStates() {
        long now = System.currentTimeMillis();
        return Arrays.asList(
                new SyncStateEntity("catalog", "seed-v1", now),
                new SyncStateEntity("profile", "seed-v1", now)
        );
    }
}
