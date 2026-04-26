package hcmute.edu.vn.nguyenthetan.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.core.di.DatabaseModule;
import hcmute.edu.vn.nguyenthetan.core.di.NetworkModule;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteNotificationSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteProgressSyncManager;

/**
 * Worker class that periodically runs when network is connected to submit any
 * offline actions (e.g. progress updates, comments) that failed previously.
 */
public class OfflineSubmitWorker extends Worker {

    private static final String TAG = "OfflineSubmitWorker";
    private static final String UNIQUE_WORK_NAME = "offline-progress-submit";

    public OfflineSubmitWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueue(@NonNull Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(OfflineSubmitWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting offline submission sync...");
        TungTungDatabase database = DatabaseModule.createDatabase(getApplicationContext());
        try {
            UserSessionStore userSessionStore = new UserSessionStore(getApplicationContext());
            MobileApiService mobileApiService = NetworkModule.createMobileApiService(userSessionStore);
            RemoteProgressSyncManager progressSyncManager = new RemoteProgressSyncManager(
                    getApplicationContext(),
                    mobileApiService,
                    database,
                    userSessionStore
            );
            RemoteNotificationSyncManager notificationSyncManager = new RemoteNotificationSyncManager(
                    getApplicationContext(),
                    mobileApiService,
                    database,
                    userSessionStore
            );
            boolean progressSynced = progressSyncManager.flushPendingActions();
            boolean notificationsSynced = notificationSyncManager.flushPendingReminderDeliveries();
            return progressSynced && notificationsSynced
                    ? Result.success()
                    : Result.retry();
        } finally {
            database.close();
        }
    }
}
