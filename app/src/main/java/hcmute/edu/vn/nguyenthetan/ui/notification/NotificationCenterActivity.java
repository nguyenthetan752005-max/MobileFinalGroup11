package hcmute.edu.vn.nguyenthetan.ui.notification;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.core.NotificationPreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationFeed;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityNotificationCenterBinding;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;

public class NotificationCenterActivity extends ThemedActivity implements NotificationAdapter.OnNotificationClickListener {

    private static final int NOTIFICATION_LIMIT = 50;

    private ActivityNotificationCenterBinding binding;
    private NotificationAdapter adapter;
    private UserSessionStore userSessionStore;
    private NotificationCenterViewModel viewModel;
    private final List<NotificationItem> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TungTungApplication application = (TungTungApplication) getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();
        NotificationCenterViewModelFactory factory = new NotificationCenterViewModelFactory(
                application.getAppContainer().getNotificationsUseCase(),
                application.getAppContainer().getMarkAllNotificationsReadUseCase(),
                application.getAppContainer().getMarkNotificationReadUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(NotificationCenterViewModel.class);

        adapter = new NotificationAdapter(this);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotifications.setAdapter(adapter);
        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonMarkAllRead.setOnClickListener(v -> viewModel.markAllRead());

        viewModel.getFeedState().observe(this, this::renderFeedState);
        viewModel.getMarkAllInFlight().observe(this, inFlight -> {
            if (binding == null) return;
            binding.buttonMarkAllRead.setEnabled(!Boolean.TRUE.equals(inFlight));
        });
        viewModel.getMarkAllSucceeded().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                markAllNotificationsReadLocally();
            }
        });
        viewModel.getSingleMarkRead().observe(this, this::markNotificationReadLocally);

        if (userSessionStore == null || !userSessionStore.isLoggedIn()) {
            renderNotifications(buildGuestNotifications(), false, R.string.notifications_empty);
        } else {
            setLoading(true);
            viewModel.load(NOTIFICATION_LIMIT);
        }
    }

    private void renderFeedState(NotificationCenterViewModel.FeedState state) {
        if (binding == null) return;
        if (state.loading) {
            setLoading(true);
            return;
        }
        if (state.failed) {
            renderNotifications(new ArrayList<>(), false, R.string.notifications_load_failed);
            return;
        }
        NotificationFeed feed = state.feed;
        if (feed == null) {
            renderNotifications(new ArrayList<>(), false, R.string.notifications_empty);
            return;
        }
        renderNotifications(mapRemoteNotifications(feed.items), feed.unreadCount > 0L, R.string.notifications_empty);
    }

    @NonNull
    private List<NotificationItem> buildGuestNotifications() {
        List<NotificationItem> guestNotifications = new ArrayList<>();
        guestNotifications.add(buildMoodNotification());
        guestNotifications.add(buildReminderNotification());
        guestNotifications.add(new NotificationItem(
                -1L,
                NotificationItem.Type.REPLY,
                getString(R.string.notification_type_reply),
                getString(R.string.notification_guest_hint),
                "",
                true,
                -1L
        ));
        return guestNotifications;
    }

    @NonNull
    private NotificationItem buildMoodNotification() {
        MascotMoodResolver.Mood mood = AppearancePreferenceStore.getCurrentMood(this);
        return new NotificationItem(
                -1L,
                NotificationItem.Type.MOOD,
                getString(R.string.notification_type_mood),
                getString(R.string.notification_mood_body, mood.getLabel()),
                mood.getLabel(),
                true,
                -1L
        );
    }

    @NonNull
    private NotificationItem buildReminderNotification() {
        boolean enabled = NotificationPreferenceStore.isEnabled(this);
        String reminderTime = ReminderSettingsStore.getDailyReminderTime(this);
        String reminderTimezone = formatTimezone(ReminderSettingsStore.getDailyReminderTimezone(this));
        String body = enabled
                ? getString(R.string.notification_reminder_enabled_body, reminderTime, reminderTimezone)
                : getString(R.string.notification_reminder_disabled_body);
        return new NotificationItem(
                -1L,
                NotificationItem.Type.REMINDER,
                getString(R.string.notification_type_reminder),
                body,
                enabled ? reminderTime : "",
                true,
                -1L
        );
    }

    private void setLoading(boolean loading) {
        binding.progressNotifications.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.recyclerNotifications.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.buttonMarkAllRead.setEnabled(!loading);
        if (loading) {
            binding.textNotificationsEmpty.setVisibility(View.GONE);
        }
    }

    private void renderNotifications(
            @NonNull List<NotificationItem> items,
            boolean showMarkAllRead,
            int emptyMessageRes
    ) {
        notifications.clear();
        notifications.addAll(items);
        adapter.submitList(new ArrayList<>(notifications));
        binding.buttonMarkAllRead.setVisibility(showMarkAllRead ? View.VISIBLE : View.GONE);
        binding.textNotificationsEmpty.setText(emptyMessageRes);
        binding.textNotificationsEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        setLoading(false);
    }

    @NonNull
    private List<NotificationItem> mapRemoteNotifications(List<hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationItem> items) {
        List<NotificationItem> mappedItems = new ArrayList<>();
        if (items == null) {
            return mappedItems;
        }
        for (hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationItem item : items) {
            if (item == null) continue;
            mappedItems.add(new NotificationItem(
                    item.id,
                    mapType(item.type),
                    safeText(item.title, getString(R.string.notifications_title)),
                    safeText(item.body, getString(R.string.notification_reply_fallback_body)),
                    buildMeta(item.meta, item.timeAgo),
                    Boolean.TRUE.equals(item.read),
                    item.targetLessonId == null ? -1L : item.targetLessonId
            ));
        }
        return mappedItems;
    }

    @NonNull
    private NotificationItem.Type mapType(String rawType) {
        if ("DAILY_REMINDER".equalsIgnoreCase(rawType)) return NotificationItem.Type.REMINDER;
        if ("COMMENT_REPLY".equalsIgnoreCase(rawType)) return NotificationItem.Type.REPLY;
        return NotificationItem.Type.MOOD;
    }

    @NonNull
    private String buildMeta(String meta, String timeAgo) {
        String safeMeta = safeText(meta, "");
        String safeTimeAgo = safeText(timeAgo, "");
        if (safeMeta.isEmpty()) return safeTimeAgo;
        if (safeTimeAgo.isEmpty()) return safeMeta;
        return safeMeta + " • " + safeTimeAgo;
    }

    private void markAllNotificationsReadLocally() {
        if (binding == null) return;
        List<NotificationItem> updatedItems = new ArrayList<>();
        for (NotificationItem item : notifications) {
            updatedItems.add(item.markRead());
        }
        renderNotifications(updatedItems, false, R.string.notifications_empty);
    }

    @Override
    public void onNotificationClick(@NonNull NotificationItem item) {
        if (item.isRemote() && !item.isRead()) {
            viewModel.markRead(item.getId());
        }
        if (item.getTargetLessonId() > 0L) {
            startActivity(LessonActivity.newIntent(this, item.getTargetLessonId()));
        }
    }

    private void markNotificationReadLocally(Long notificationIdBoxed) {
        if (binding == null || notificationIdBoxed == null) return;
        long notificationId = notificationIdBoxed;
        List<NotificationItem> updatedItems = new ArrayList<>();
        boolean hasUnread = false;
        for (NotificationItem item : notifications) {
            NotificationItem resolvedItem = item.getId() == notificationId ? item.markRead() : item;
            updatedItems.add(resolvedItem);
            if (!resolvedItem.isRead()) hasUnread = true;
        }
        renderNotifications(updatedItems, hasUnread, R.string.notifications_empty);
    }

    @NonNull
    private String formatTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return TimeZone.getDefault().getID().replace('_', ' ');
        }
        return timezone.trim().replace('_', ' ');
    }

    @NonNull
    private String safeText(String value, @NonNull String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
