package hcmute.edu.vn.nguyenthetan.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentHomeBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;

public class HomeFragment extends Fragment {

    public interface Listener {
        void onContinueLearning(long lessonId);

        void onOpenStreakDialog();

        void onOpenSettings();
    }

    private FragmentHomeBinding binding;
    private RecommendationAdapter recommendationAdapter;
    private Listener listener;
    private long currentLessonId;
    private UserSessionStore userSessionStore;

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

        recommendationAdapter = new RecommendationAdapter();
        binding.recyclerRecommendations.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerRecommendations.setAdapter(recommendationAdapter);

        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();
        HomeViewModelFactory factory = new HomeViewModelFactory(application.getAppContainer().getHomeDashboardUseCase());
        HomeViewModel viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        viewModel.getDashboardState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), loading ->
                binding.progressHomeLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        application.getAppContainer().getSyncErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        
        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (!syncing) {
                viewModel.forceLoad();
            }
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
        binding.cardStreak.setOnClickListener(v -> openStreakDialog());
        binding.buttonStreak.setOnClickListener(v -> openStreakDialog());
        binding.buttonNotification.setOnClickListener(v -> openSettings());
    }

    private void render(HomeDashboard dashboard) {
        boolean guestMode = userSessionStore == null || !userSessionStore.isLoggedIn();
        currentLessonId = dashboard.getCurrentLesson().getLessonId();
        binding.textGreeting.setText(dashboard.getGreeting());
        binding.textSubtitle.setText(dashboard.getSubtitle());
        binding.cardGuestPromo.setVisibility(guestMode ? View.VISIBLE : View.GONE);
        binding.textGuestPromoTitle.setText(R.string.home_guest_promo_title);
        binding.textGuestPromoBody.setText(R.string.home_guest_promo_body);
        binding.textStreakValue.setText(guestMode
                ? getString(R.string.home_guest_streak_value)
                : String.format(Locale.US, "%d days", dashboard.getStreakSummary().getCurrentDays()));
        binding.textMood.setText(guestMode
                ? getString(R.string.home_guest_mood)
                : dashboard.getStreakSummary().getActiveMood());
        binding.textCurrentLessonTitle.setText(dashboard.getCurrentLesson().getTitle());
        binding.progressCurrentLesson.setMax(Math.max(1, dashboard.getCurrentLesson().getTotalSentences()));
        binding.progressCurrentLesson.setProgressCompat(guestMode ? 0 : dashboard.getCurrentLesson().getCompletedSentences(), true);
        binding.textLessonProgress.setText(guestMode
                ? getString(R.string.home_guest_progress)
                : getString(
                R.string.lesson_progress_format,
                dashboard.getCurrentLesson().getCompletedSentences(),
                dashboard.getCurrentLesson().getTotalSentences()
        ));
        binding.chipPrimaryMode.setText(guestMode ? getString(R.string.home_guest_streak_title) : dashboard.getCurrentLesson().getPrimaryMode());
        binding.chipSecondaryMode.setText(guestMode ? getString(R.string.button_create_account) : dashboard.getCurrentLesson().getSecondaryMode());
        binding.textTodayValue.setText(guestMode ? getString(R.string.home_guest_today) : dashboard.getStudyStats().getToday());
        binding.textWeekValue.setText(guestMode ? getString(R.string.home_guest_week) : dashboard.getStudyStats().getThisWeek());
        binding.textBestValue.setText(guestMode ? getString(R.string.home_guest_best) : String.valueOf(dashboard.getStudyStats().getBestStreak()));
        renderStreakDots(guestMode ? java.util.Collections.emptyList() : dashboard.getStreakSummary().getWeekStatus());
        binding.buttonContinue.setText(guestMode ? R.string.button_start_listening : R.string.button_continue);
        recommendationAdapter.submitList(dashboard.getRecommendations());
    }

    private void renderStreakDots(List<Boolean> statuses) {
        binding.streakDotsContainer.removeAllViews();
        int dotSize = dp(18);
        int marginEnd = dp(8);
        for (boolean studied : statuses) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMarginEnd(marginEnd);
            dot.setLayoutParams(params);

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(studied
                    ? ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorPrimary)
                    : ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorSurface));
            background.setStroke(dp(2), studied
                    ? ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorPrimary)
                    : ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorBorder));
            dot.setBackground(background);

            binding.streakDotsContainer.addView(dot);
        }
    }

    private void openStreakDialog() {
        if (userSessionStore == null || !userSessionStore.isLoggedIn()) {
            Toast.makeText(requireContext(), R.string.guest_sign_in_prompt, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
            return;
        }
        if (listener != null) {
            listener.onOpenStreakDialog();
        }
    }

    private void openSettings() {
        if (listener != null) {
            listener.onOpenSettings();
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
