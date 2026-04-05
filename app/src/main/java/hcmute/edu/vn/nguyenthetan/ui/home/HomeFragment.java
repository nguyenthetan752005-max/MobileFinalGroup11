package hcmute.edu.vn.nguyenthetan.ui.home;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import hcmute.edu.vn.nguyenthetan.databinding.FragmentHomeBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;

public class HomeFragment extends Fragment {

    public interface Listener {
        void onContinueLearning(long lessonId);

        void onOpenStreakDialog();
    }

    private FragmentHomeBinding binding;
    private RecommendationAdapter recommendationAdapter;
    private Listener listener;
    private long currentLessonId;

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
        HomeViewModelFactory factory = new HomeViewModelFactory(application.getAppContainer().getHomeDashboardUseCase());
        HomeViewModel viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        viewModel.getDashboardState().observe(getViewLifecycleOwner(), this::render);
        
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
        binding.cardStreak.setOnClickListener(v -> openStreakDialog());
        binding.buttonStreak.setOnClickListener(v -> openStreakDialog());
    }

    private void render(HomeDashboard dashboard) {
        currentLessonId = dashboard.getCurrentLesson().getLessonId();
        binding.textGreeting.setText(dashboard.getGreeting());
        binding.textSubtitle.setText(dashboard.getSubtitle());
        binding.textStreakValue.setText(String.format(Locale.US, "%d days", dashboard.getStreakSummary().getCurrentDays()));
        binding.textMood.setText(dashboard.getStreakSummary().getActiveMood());
        binding.textCurrentLessonTitle.setText(dashboard.getCurrentLesson().getTitle());
        binding.progressCurrentLesson.setMax(dashboard.getCurrentLesson().getTotalSentences());
        binding.progressCurrentLesson.setProgressCompat(dashboard.getCurrentLesson().getCompletedSentences(), true);
        binding.textLessonProgress.setText(
                String.format(
                        Locale.US,
                        "%d/%d sentences",
                        dashboard.getCurrentLesson().getCompletedSentences(),
                        dashboard.getCurrentLesson().getTotalSentences()
                )
        );
        binding.chipPrimaryMode.setText(dashboard.getCurrentLesson().getPrimaryMode());
        binding.chipSecondaryMode.setText(dashboard.getCurrentLesson().getSecondaryMode());
        binding.textTodayValue.setText(dashboard.getStudyStats().getToday());
        binding.textWeekValue.setText(dashboard.getStudyStats().getThisWeek());
        binding.textBestValue.setText(String.valueOf(dashboard.getStudyStats().getBestStreak()));
        renderStreakDots(dashboard.getStreakSummary().getWeekStatus());
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
            background.setColor(ContextCompat.getColor(requireContext(), studied ? R.color.tt_primary : R.color.tt_surface));
            background.setStroke(dp(2), ContextCompat.getColor(requireContext(), studied ? R.color.tt_primary : R.color.tt_border));
            dot.setBackground(background);

            binding.streakDotsContainer.addView(dot);
        }
    }

    private void openStreakDialog() {
        if (listener != null) {
            listener.onOpenStreakDialog();
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
