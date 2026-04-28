package hcmute.edu.vn.nguyenthetan.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.core.di.DatabaseModule;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;

public final class DailyReminderNotificationHelper {

    private static final int NOTIFICATION_ID = 1107;
    private static final int REQUEST_CODE_CONTENT = 11070;
    private static final int REQUEST_CODE_CONTINUE_LESSON = 11071;
    private static final int REQUEST_CODE_OPEN_APP = 11072;

    private DailyReminderNotificationHelper() {
    }

    @NonNull
    public static ReminderNotificationRecord showReminderNotification(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        NotificationTargets targets = resolveNotificationTargets(appContext);
        MascotMoodResolver.Mood mood = targets.mood;
        String notificationTitle = appContext.getString(MascotMoodResolver.getReminderTitleRes(mood));
        String notificationBody = buildNotificationBody(appContext, mood, targets.lessonTitle);

        DailyReminderScheduler.createNotificationChannel(appContext);

        Bitmap mascotBitmap = BitmapFactory.decodeResource(appContext.getResources(), mood.getDrawableRes());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, DailyReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(mascotBitmap)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(targets.contentIntent);
        if (targets.lessonTitle != null && !targets.lessonTitle.trim().isEmpty()) {
            builder.setSubText(appContext.getString(R.string.notification_current_lesson_label, targets.lessonTitle.trim()));
        }

        if (targets.continueLessonIntent != null) {
            builder.addAction(
                    R.drawable.ic_play,
                    appContext.getString(R.string.notification_action_continue_lesson),
                    targets.continueLessonIntent
            );
        }
        builder.addAction(
                R.drawable.ic_nav_home,
                appContext.getString(R.string.notification_action_open_app),
                targets.openAppIntent
        );

        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, builder.build());
        return new ReminderNotificationRecord(
                notificationTitle,
                notificationBody,
                targets.lessonId,
                targets.lessonTitle
        );
    }

    @NonNull
    public static MascotMoodResolver.Mood resolveCurrentMood(@NonNull Context context) {
        TungTungDatabase database = DatabaseModule.createDatabase(context.getApplicationContext());
        try {
            ProfileEntity profile;
            try {
                profile = database.profileDao().getProfile();
            } catch (IllegalStateException e) {
                // Room forbids main-thread queries; return default mood gracefully
                profile = null;
            }
            return MascotMoodResolver.resolve(profile);
        } finally {
            database.close();
        }
    }

    @NonNull
    private static NotificationTargets resolveNotificationTargets(@NonNull Context context) {
        TungTungDatabase database = DatabaseModule.createDatabase(context.getApplicationContext());
        try {
            ProfileEntity profile = database.profileDao().getProfile();
            MascotMoodResolver.Mood mood = MascotMoodResolver.resolve(profile);
            PendingIntent openAppIntent = buildPendingIntent(
                    context,
                    buildMainIntent(context),
                    REQUEST_CODE_OPEN_APP
            );

            PendingIntent continueLessonIntent = null;
            PendingIntent contentIntent = openAppIntent;
            String lessonTitle = null;
            Long lessonId = null;

            AppSettingsEntity settings = database.appSettingsDao().getSettings();
            if (settings != null && settings.lastOpenedLessonId != null) {
                LessonEntity lesson = database.lessonDao().getById(settings.lastOpenedLessonId);
                if (lesson != null) {
                    lessonId = lesson.id;
                    lessonTitle = lesson.title;
                    continueLessonIntent = buildPendingIntent(
                            context,
                            buildLessonIntent(context, lesson.id),
                            REQUEST_CODE_CONTINUE_LESSON
                    );
                    contentIntent = buildPendingIntent(
                            context,
                            buildLessonIntent(context, lesson.id),
                            REQUEST_CODE_CONTENT
                    );
                }
            }
            return new NotificationTargets(mood, lessonId, lessonTitle, contentIntent, continueLessonIntent, openAppIntent);
        } finally {
            database.close();
        }
    }

    @NonNull
    private static String buildNotificationBody(
            @NonNull Context context,
            @NonNull MascotMoodResolver.Mood mood,
            String lessonTitle
    ) {
        String defaultBody = context.getString(MascotMoodResolver.getReminderBodyRes(mood));
        if (lessonTitle == null || lessonTitle.trim().isEmpty()) {
            return defaultBody;
        }
        return context.getString(
                R.string.daily_reminder_notification_body_with_lesson,
                lessonTitle.trim(),
                defaultBody
        );
    }

    @NonNull
    private static PendingIntent buildPendingIntent(@NonNull Context context, @NonNull Intent intent, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @NonNull
    private static Intent buildMainIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @NonNull
    private static Intent buildLessonIntent(@NonNull Context context, long lessonId) {
        Intent intent = LessonActivity.newIntent(context, lessonId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private static final class NotificationTargets {
        private final MascotMoodResolver.Mood mood;
        private final Long lessonId;
        private final String lessonTitle;
        private final PendingIntent contentIntent;
        private final PendingIntent continueLessonIntent;
        private final PendingIntent openAppIntent;

        private NotificationTargets(
                MascotMoodResolver.Mood mood,
                Long lessonId,
                String lessonTitle,
                PendingIntent contentIntent,
                PendingIntent continueLessonIntent,
                PendingIntent openAppIntent
        ) {
            this.mood = mood;
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
            this.contentIntent = contentIntent;
            this.continueLessonIntent = continueLessonIntent;
            this.openAppIntent = openAppIntent;
        }
    }

    public static final class ReminderNotificationRecord {
        public final String title;
        public final String body;
        public final Long lessonId;
        public final String lessonTitle;

        private ReminderNotificationRecord(String title, String body, Long lessonId, String lessonTitle) {
            this.title = title;
            this.body = body;
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
        }
    }
}
