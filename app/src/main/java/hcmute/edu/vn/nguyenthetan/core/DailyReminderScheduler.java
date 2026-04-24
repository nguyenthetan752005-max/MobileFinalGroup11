package hcmute.edu.vn.nguyenthetan.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.work.DailyReminderWorker;

public final class DailyReminderScheduler {

    public static final String CHANNEL_ID = "daily_study_reminder";
    public static final String UNIQUE_WORK_NAME = "daily-study-reminder";
    public static final String INPUT_REMINDER_TIME = "input_reminder_time";
    public static final String INPUT_REMINDER_TIMEZONE = "input_reminder_timezone";
    private static final String DEFAULT_REMINDER_TIME = "19:00";
    private static final String DEFAULT_REMINDER_TIMEZONE = "Asia/Ho_Chi_Minh";

    private DailyReminderScheduler() {
    }

    public static void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
        if (existingChannel != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.daily_reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.daily_reminder_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    public static void schedule(@NonNull Context context, String reminderTime, String reminderTimezone) {
        createNotificationChannel(context);

        String safeTime = sanitizeTime(reminderTime);
        String safeTimezone = sanitizeTimezone(reminderTimezone);
        long delayMillis = computeInitialDelayMillis(safeTime, safeTimezone);

        Data inputData = new Data.Builder()
                .putString(INPUT_REMINDER_TIME, safeTime)
                .putString(INPUT_REMINDER_TIMEZONE, safeTimezone)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DailyReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    public static void cancel(@NonNull Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
    }

    public static void apply(@NonNull Context context) {
        if (!NotificationPreferenceStore.isEnabled(context)
                || !ReminderSettingsStore.isDailyReminderEnabled(context)
                || !hasNotificationPermission(context)) {
            cancel(context);
            return;
        }
        schedule(
                context,
                ReminderSettingsStore.getDailyReminderTime(context),
                ReminderSettingsStore.getDailyReminderTimezone(context)
        );
    }

    public static boolean hasNotificationPermission(@NonNull Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static long computeInitialDelayMillis(String reminderTime, String reminderTimezone) {
        String[] parts = reminderTime.split(":");
        int hour = parts.length > 0 ? parseIntOrDefault(parts[0], 19) : 19;
        int minute = parts.length > 1 ? parseIntOrDefault(parts[1], 0) : 0;

        TimeZone timeZone = TimeZone.getTimeZone(sanitizeTimezone(reminderTimezone));
        Calendar now = Calendar.getInstance(timeZone);
        Calendar trigger = Calendar.getInstance(timeZone);
        trigger.setTimeInMillis(now.getTimeInMillis());
        trigger.set(Calendar.HOUR_OF_DAY, hour);
        trigger.set(Calendar.MINUTE, minute);
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);
        if (!trigger.after(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = trigger.getTimeInMillis() - System.currentTimeMillis();
        return Math.max(delay, TimeUnit.MINUTES.toMillis(1));
    }

    private static String sanitizeTime(String reminderTime) {
        if (reminderTime == null || !reminderTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return DEFAULT_REMINDER_TIME;
        }
        return reminderTime;
    }

    private static String sanitizeTimezone(String reminderTimezone) {
        if (reminderTimezone == null || reminderTimezone.trim().isEmpty()) {
            return DEFAULT_REMINDER_TIMEZONE;
        }
        return reminderTimezone.trim();
    }

    private static int parseIntOrDefault(String rawValue, int fallback) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
