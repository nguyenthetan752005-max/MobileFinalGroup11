package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.ThemePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentProfileBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.DailyActivity;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;
import hcmute.edu.vn.nguyenthetan.ui.common.StreakDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    public interface Listener {
        void onOpenStreakDialog();
    }

    private FragmentProfileBinding binding;
    private UserSessionStore userSessionStore;
    private MobileApiService mobileApiService;

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
        userSessionStore = application.getAppContainer().getUserSessionStore();
        mobileApiService = application.getAppContainer().getMobileApiService();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(application.getAppContainer().getProfileUseCase());
        ProfileViewModel viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        viewModel.getProfileState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), loading ->
                binding.progressProfileLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        viewModel.load();

        binding.textThemeLight.setOnClickListener(v ->
                ThemePreferenceStore.setThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_NO)
        );
        binding.textThemeDark.setOnClickListener(v ->
                ThemePreferenceStore.setThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_YES)
        );
        binding.cardMood.setOnClickListener(v -> {
            if (userSessionStore != null && userSessionStore.isLoggedIn()) {
                showStreakDialog(getParentFragmentManager(), false);
            } else {
                promptLoginRequired();
            }
        });
        binding.buttonSessionAction.setOnClickListener(v -> handleSessionAction());
        binding.buttonGuestProfileCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OnboardingActivity.class))
        );
    }

    private void render(ProfileData profileData) {
        boolean guestMode = userSessionStore == null || !userSessionStore.isLoggedIn();
        binding.textAvatar.setText(guestMode ? "GU" : buildAvatar(profileData.getName()));
        binding.textName.setText(guestMode ? getString(R.string.guest_display_name) : profileData.getName());
        binding.textEmail.setText(guestMode ? getString(R.string.guest_profile_hint) : profileData.getEmail());
        binding.textCurrentStreak.setText(guestMode ? "--" : String.valueOf(profileData.getCurrentStreak()));
        binding.textLongestStreak.setText(guestMode ? "--" : String.valueOf(profileData.getLongestStreak()));
        binding.textTotalStudy.setText(guestMode ? "--" : profileData.getTotalStudyTime());
        binding.textMood.setText(guestMode
                ? getString(R.string.guest_profile_cta)
                : profileData.getStreakSummary().getActiveMood() + " mood");
        binding.switchNotifications.setChecked(profileData.isNotificationsEnabled());
        binding.switchNotifications.setEnabled(!guestMode);
        binding.layoutProfileStats.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardWeeklyActivity.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardMood.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardGuestProfilePromo.setVisibility(guestMode ? View.VISIBLE : View.GONE);
        binding.textGuestProfileTitle.setText(R.string.guest_profile_title);
        binding.textGuestProfileBody.setText(R.string.guest_profile_body);
        if (!guestMode) {
            renderActivityBars(profileData);
        } else {
            binding.activityChartContainer.removeAllViews();
        }
        renderThemeMode();
        renderSessionCard(profileData);
    }

    private void renderSessionCard(ProfileData profileData) {
        if (userSessionStore != null && userSessionStore.isLoggedIn()) {
            binding.textSessionStatus.setText(getString(R.string.session_logged_in, userSessionStore.getUsername()));
            binding.buttonSessionAction.setText(R.string.button_logout);
            long userId = userSessionStore.getUserId();
            if (userId > 0L && mobileApiService != null) {
                mobileApiService.getProfile(userId).enqueue(new Callback<UserProfileDto>() {
                    @Override
                    public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                        if (!isAdded() || response.body() == null || !response.isSuccessful()) {
                            return;
                        }
                        UserProfileDto body = response.body();
                        binding.textAvatar.setText(buildAvatar(body.username));
                        binding.textName.setText(body.username);
                        binding.textEmail.setText(body.email);
                        binding.textSessionStatus.setText(getString(R.string.session_backend_connected, body.username));
                    }

                    @Override
                    public void onFailure(Call<UserProfileDto> call, Throwable throwable) {
                        if (isAdded()) {
                            binding.textSessionStatus.setText(getString(R.string.session_backend_failed, throwable.getMessage()));
                        }
                    }
                });
            }
        } else {
            binding.textSessionStatus.setText(R.string.session_guest_mode);
            binding.buttonSessionAction.setText(R.string.button_sign_in);
        }
    }

    private void handleSessionAction() {
        if (userSessionStore != null && userSessionStore.isLoggedIn()) {
            userSessionStore.clear();
            startActivity(new android.content.Intent(requireContext(), OnboardingActivity.class));
            requireActivity().finish();
            return;
        }
        startActivity(new Intent(requireContext(), OnboardingActivity.class));
    }

    private void promptLoginRequired() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_title)
                .setMessage(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_message)
                .setPositiveButton(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_positive, (dialog, which) -> {
                    startActivity(new android.content.Intent(requireContext(), hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity.class));
                })
                .setNegativeButton(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_negative, null)
                .show();
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

    private String buildAvatar(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "TT";
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void renderThemeMode() {
        boolean darkMode = ThemePreferenceStore.isDarkMode(requireContext());
        int activeColor = ContextCompat.getColor(requireContext(), R.color.tt_primary);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.tt_text_secondary);
        binding.textThemeLight.setTextColor(darkMode ? inactiveColor : activeColor);
        binding.textThemeDark.setTextColor(darkMode ? activeColor : inactiveColor);
        binding.textThemeLight.setAlpha(darkMode ? 0.7f : 1f);
        binding.textThemeDark.setAlpha(darkMode ? 1f : 0.7f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
