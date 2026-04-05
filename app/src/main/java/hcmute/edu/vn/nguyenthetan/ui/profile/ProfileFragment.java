package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentProfileBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.DailyActivity;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.ui.common.StreakDialogFragment;

public class ProfileFragment extends Fragment {

    public interface Listener {
        void onOpenStreakDialog();
    }

    private FragmentProfileBinding binding;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public static void showStreakDialog(FragmentManager fragmentManager, boolean broken) {
        StreakDialogFragment.newInstance(broken).show(fragmentManager, "streak_dialog");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(application.getAppContainer().getProfileUseCase());
        ProfileViewModel viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        viewModel.getProfileState().observe(getViewLifecycleOwner(), this::render);
        viewModel.load();

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        ));
        binding.cardMood.setOnClickListener(v -> showStreakDialog(getParentFragmentManager(), false));
    }

    private void render(ProfileData profileData) {
        binding.textAvatar.setText("TN");
        binding.textName.setText(profileData.getName());
        binding.textEmail.setText(profileData.getEmail());
        binding.textCurrentStreak.setText(String.valueOf(profileData.getCurrentStreak()));
        binding.textLongestStreak.setText(String.valueOf(profileData.getLongestStreak()));
        binding.textTotalStudy.setText(profileData.getTotalStudyTime());
        binding.textMood.setText(profileData.getStreakSummary().getActiveMood() + " mood");
        binding.switchDarkMode.setChecked(profileData.isDarkModeEnabled());
        binding.switchNotifications.setChecked(profileData.isNotificationsEnabled());
        renderActivityBars(profileData);
    }

    private void renderActivityBars(ProfileData profileData) {
        binding.activityChartContainer.removeAllViews();
        for (DailyActivity activity : profileData.getDailyActivities()) {
            binding.activityChartContainer.addView(createBarView(requireContext(), activity));
        }
    }

    private View createBarView(Context context, DailyActivity activity) {
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        wrapperParams.setMargins(dp(4), 0, dp(4), 0);
        wrapper.setLayoutParams(wrapperParams);

        View bar = new View(context);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dp(18), Math.max(dp(24), dp(activity.getMinutes() * 2)));
        bar.setLayoutParams(barParams);
        bar.setBackgroundColor(ContextCompat.getColor(context, activity.isHighlighted() ? R.color.tt_primary : R.color.tt_border));
        wrapper.addView(bar);

        TextView label = new TextView(context);
        label.setText(activity.getDayLabel());
        label.setTextColor(ContextCompat.getColor(context, R.color.tt_text_secondary));
        label.setTextSize(12f);
        label.setPadding(0, dp(8), 0, 0);
        wrapper.addView(label);

        return wrapper;
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
