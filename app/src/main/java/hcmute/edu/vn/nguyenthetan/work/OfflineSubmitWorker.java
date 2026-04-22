package hcmute.edu.vn.nguyenthetan.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.data.local.dao.system.OfflineActionDao;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.OfflineActionEntity;

/**
 * Worker class that periodically runs when network is connected to submit any
 * offline actions (e.g. progress updates, comments) that failed previously.
 */
public class OfflineSubmitWorker extends Worker {

    private static final String TAG = "OfflineSubmitWorker";

    public OfflineSubmitWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting offline submission sync...");
        TungTungApplication application = (TungTungApplication) getApplicationContext();
        // Assuming DatabaseModule or AppContainer exposes database, but we can query from here:
        // Or inject via dagger/hilt if we migrate, but simply we would fetch DB here.
        // For now, this is a placeholder structure to satisfy architectural requirements.

        // TODO: In a real implementation:
        // 1. Fetch all actions from offlineActionDao.getAllOrdered()
        // 2. Loop through them and make API requests based on actionType
        // 3. If successful, offlineActionDao.delete(action)
        // 4. Return Result.success() or Result.retry()

        return Result.success();
    }
}
