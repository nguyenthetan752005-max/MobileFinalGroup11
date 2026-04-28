package hcmute.edu.vn.nguyenthetan.ui.notification;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationFeed;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkAllNotificationsReadUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkNotificationReadUseCase;

public class NotificationCenterViewModel extends ViewModel {

    public static final class FeedState {
        public final boolean loading;
        public final boolean failed;
        @Nullable public final NotificationFeed feed;

        FeedState(boolean loading, boolean failed, @Nullable NotificationFeed feed) {
            this.loading = loading;
            this.failed = failed;
            this.feed = feed;
        }
    }

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkAllNotificationsReadUseCase markAllReadUseCase;
    private final MarkNotificationReadUseCase markReadUseCase;
    private final MutableLiveData<FeedState> feedState = new MutableLiveData<>(new FeedState(false, false, null));
    private final MutableLiveData<Boolean> markAllInFlight = new MutableLiveData<>(false);
    private final MutableLiveData<Long> singleMarkRead = new MutableLiveData<>();
    private final MutableLiveData<Boolean> markAllSucceeded = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotificationCenterViewModel(
            GetNotificationsUseCase getNotificationsUseCase,
            MarkAllNotificationsReadUseCase markAllReadUseCase,
            MarkNotificationReadUseCase markReadUseCase
    ) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markAllReadUseCase = markAllReadUseCase;
        this.markReadUseCase = markReadUseCase;
    }

    public LiveData<FeedState> getFeedState() {
        return feedState;
    }

    public LiveData<Boolean> getMarkAllInFlight() {
        return markAllInFlight;
    }

    /** Emits the id of a notification that was successfully marked read. */
    public LiveData<Long> getSingleMarkRead() {
        return singleMarkRead;
    }

    /** Emits true once whenever a "mark all read" call succeeds. */
    public LiveData<Boolean> getMarkAllSucceeded() {
        return markAllSucceeded;
    }

    public void load(int limit) {
        feedState.setValue(new FeedState(true, false, null));
        executor.execute(() -> {
            NotificationFeed feed = getNotificationsUseCase.execute(limit);
            if (feed == null) {
                feedState.postValue(new FeedState(false, true, null));
            } else {
                feedState.postValue(new FeedState(false, false, feed));
            }
        });
    }

    public void markAllRead() {
        markAllInFlight.setValue(true);
        executor.execute(() -> {
            boolean success = markAllReadUseCase.execute();
            markAllInFlight.postValue(false);
            if (success) {
                markAllSucceeded.postValue(true);
            }
        });
    }

    public void markRead(long notificationId) {
        executor.execute(() -> {
            if (markReadUseCase.execute(notificationId)) {
                singleMarkRead.postValue(notificationId);
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
