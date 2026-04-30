package hcmute.edu.vn.nguyenthetan.ui.notification;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.DeleteNotificationUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkAllNotificationsReadUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.MarkNotificationReadUseCase;

public class NotificationCenterViewModelFactory implements ViewModelProvider.Factory {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkAllNotificationsReadUseCase markAllReadUseCase;
    private final MarkNotificationReadUseCase markReadUseCase;
    private final DeleteNotificationUseCase deleteUseCase;

    public NotificationCenterViewModelFactory(
            GetNotificationsUseCase getNotificationsUseCase,
            MarkAllNotificationsReadUseCase markAllReadUseCase,
            MarkNotificationReadUseCase markReadUseCase,
            DeleteNotificationUseCase deleteUseCase
    ) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markAllReadUseCase = markAllReadUseCase;
        this.markReadUseCase = markReadUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationCenterViewModel.class)) {
            return (T) new NotificationCenterViewModel(getNotificationsUseCase, markAllReadUseCase, markReadUseCase, deleteUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
