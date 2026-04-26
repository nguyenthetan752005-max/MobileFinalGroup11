package hcmute.edu.vn.nguyenthetan.work;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import hcmute.edu.vn.nguyenthetan.core.DailyReminderNotificationHelper;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderScheduler;
import hcmute.edu.vn.nguyenthetan.core.NotificationPreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.core.di.DatabaseModule;
import hcmute.edu.vn.nguyenthetan.core.di.NetworkModule;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteNotificationSyncManager;

public class DailyReminderWorker extends Worker {

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String reminderTime = getInputData().getString(DailyReminderScheduler.INPUT_REMINDER_TIME);
        String reminderTimezone = getInputData().getString(DailyReminderScheduler.INPUT_REMINDER_TIMEZONE);

        if (!NotificationPreferenceStore.isEnabled(context)
                || !ReminderSettingsStore.isDailyReminderEnabled(context)) {
            DailyReminderScheduler.cancel(context);
            return Result.success();
        }

        DailyReminderScheduler.createNotificationChannel(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            DailyReminderScheduler.cancel(context);
            return Result.success();
        }

        DailyReminderNotificationHelper.ReminderNotificationRecord reminderRecord =
                DailyReminderNotificationHelper.showReminderNotification(context);
        recordReminderDelivery(context, reminderTime, reminderTimezone, reminderRecord);

        DailyReminderScheduler.schedule(context, reminderTime, reminderTimezone);
        return Result.success();
    }

    private void recordReminderDelivery(
            @NonNull Context context,
            String reminderTime,
            String reminderTimezone,
            DailyReminderNotificationHelper.ReminderNotificationRecord reminderRecord
    ) {
        UserSessionStore userSessionStore = new UserSessionStore(context);
        if (!userSessionStore.isLoggedIn() || reminderRecord == null) {
            return;
        }

        TungTungDatabase database = DatabaseModule.createDatabase(context);
        try {
            MobileApiService mobileApiService = NetworkModule.createMobileApiService(userSessionStore);
            RemoteNotificationSyncManager notificationSyncManager = new RemoteNotificationSyncManager(
                    context,
                    mobileApiService,
                    database,
                    userSessionStore
            );
            notificationSyncManager.recordReminderDelivery(
                    reminderRecord.title,
                    reminderRecord.body,
                    reminderTime,
                    reminderTimezone,
                    reminderRecord.lessonId,
                    reminderRecord.lessonTitle,
                    System.currentTimeMillis()
            );
        } finally {
            database.close();
        }
    }
}
