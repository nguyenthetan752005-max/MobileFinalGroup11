package hcmute.edu.vn.nguyenthetan.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentHomeBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.ui.notification.NotificationCenterActivity;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;

public class HomeFragment extends Fragment {

    public interface Listener {
        void onContinueLearning(long lessonId);
    }

    private FragmentHomeBinding binding;
    private Listener listener;
    private long currentLessonId;
    private UserSessionStore userSessionStore;
    private HomeViewModel viewModel;
    private Boolean lastSyncing;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();

        HomeViewModelFactory factory = new HomeViewModelFactory(
                application.getAppContainer().getHomeDashboardUseCase(),
                application.getAppContainer().getNotificationSummaryUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);

        viewModel.getDashboardState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), loading ->
                binding.progressHomeLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        viewModel.getUnreadNotificationCount().observe(getViewLifecycleOwner(), this::renderNotificationBadge);

        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (Boolean.TRUE.equals(lastSyncing) && !Boolean.TRUE.equals(syncing)) {
                viewModel.forceLoad();
            }
            lastSyncing = syncing;
        });

        viewModel.load();

        binding.buttonContinue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinueLearning(currentLessonId);
            }
        });
        binding.buttonGuestCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OnboardingActivity.class))
        );
        binding.buttonNotification.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationCenterActivity.class))
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshNotificationBadge(userSessionStore != null && userSessionStore.isLoggedIn());
        }
    }

    private void render(HomeDashboard dashboard) {
        boolean guestMode = userSessionStore == null || !userSessionStore.isLoggedIn();

        binding.textGreeting.setText(dashboard.getGreeting());
        String subtitle = dashboard.getSubtitle();
        boolean hasSubtitle = subtitle != null && !subtitle.trim().isEmpty();
        binding.textSubtitle.setVisibility(hasSubtitle ? View.VISIBLE : View.GONE);
        binding.textSubtitle.setText(hasSubtitle ? subtitle : "");

        binding.cardGuestPromo.setVisibility(guestMode ? View.VISIBLE : View.GONE);
        binding.textGuestPromoTitle.setText(R.string.home_guest_promo_title);
        binding.textGuestPromoBody.setText(R.string.home_guest_promo_body);

        binding.cardStreak.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.textStreakValue.setText(String.format(Locale.US, "%d days", dashboard.getStreakSummary().getCurrentDays()));

        MascotMoodResolver.Mood activeMood = dashboard.getStreakSummary().getActiveMood();
        binding.textMood.setText(dashboard.getStreakSummary().getActiveMoodLabel());

        if (!guestMode && dashboard.getCurrentLesson() != null) {
            binding.layoutContinueLearning.setVisibility(View.VISIBLE);
            currentLessonId = dashboard.getCurrentLesson().getLessonId();
            binding.textCurrentLessonTitle.setText(dashboard.getCurrentLesson().getTitle());
            binding.progressCurrentLesson.setMax(Math.max(1, dashboard.getCurrentLesson().getTotalSentences()));
            binding.progressCurrentLesson.setProgressCompat(dashboard.getCurrentLesson().getCompletedSentences(), true);

            binding.textLessonProgress.setText(getString(
                    R.string.lesson_progress_format,
                    dashboard.getCurrentLesson().getCompletedSentences(),
                    dashboard.getCurrentLesson().getTotalSentences()
            ));

            binding.chipPrimaryMode.setText(dashboard.getCurrentLesson().getPrimaryMode());
            binding.chipSecondaryMode.setText(dashboard.getCurrentLesson().getSecondaryMode());
            binding.buttonContinue.setText(R.string.button_continue);
        } else {
            binding.layoutContinueLearning.setVisibility(View.GONE);
        }

        binding.layoutStudyStats.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.textTodayValue.setText(dashboard.getStudyStats().getToday());
        binding.textWeekValue.setText(dashboard.getStudyStats().getThisWeek());
        binding.textBestValue.setText(String.valueOf(dashboard.getStudyStats().getBestStreak()));

        if (activeMood != null) {
            binding.imageMascot.setImageResource(activeMood.getDrawableRes());
            binding.textMood.setText(activeMood.getLabel());
        }

        binding.layoutHomeContent.setVisibility(View.VISIBLE);
    }

    private void renderNotificationBadge(Long unreadCountBoxed) {
        if (binding == null) return;
        long unreadCount = unreadCountBoxed == null ? 0L : unreadCountBoxed;
        if (unreadCount <= 0L) {
            binding.textNotificationBadge.setVisibility(View.GONE);
            return;
        }
        binding.textNotificationBadge.setVisibility(View.VISIBLE);
        binding.textNotificationBadge.setText(unreadCount > 9L ? "9+" : String.valueOf(unreadCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastSyncing = null;
        binding = null;
    }
}
