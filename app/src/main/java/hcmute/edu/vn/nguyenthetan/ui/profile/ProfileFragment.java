package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppPalette;
import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderScheduler;
import hcmute.edu.vn.nguyenthetan.core.NotificationPreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.core.ThemePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileReminderSettingsDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;
import hcmute.edu.vn.nguyenthetan.databinding.DialogAppearanceSettingsBinding;
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
    private String displayedUsername;
    private boolean syncingNotificationSwitch;
    private boolean remoteSettingsLoaded;
    private boolean remoteSettingsSyncFailed;
    private Boolean remoteDailyReminderEnabled;
    private String remoteDailyReminderTime;
    private String remoteDailyReminderTimezone;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!isAdded() || binding == null) {
                    return;
                }
                NotificationPreferenceStore.markPermissionRequested(requireContext());
                if (granted) {
                    NotificationPreferenceStore.setEnabled(requireContext(), true);
                    DailyReminderScheduler.apply(requireContext());
                    Toast.makeText(requireContext(), R.string.settings_notifications_enabled_toast, Toast.LENGTH_SHORT).show();
                } else {
                    NotificationPreferenceStore.setEnabled(requireContext(), false);
                    DailyReminderScheduler.cancel(requireContext());
                    Toast.makeText(requireContext(), R.string.settings_notifications_permission_denied, Toast.LENGTH_LONG).show();
                }
                renderSettingsState();
            });

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

        binding.actionEditProfile.setOnClickListener(v -> {
            if (userSessionStore != null && userSessionStore.isLoggedIn()) {
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
            } else {
                promptLoginRequired();
            }
        });

        binding.actionMyComments.setOnClickListener(v -> {
            if (userSessionStore != null && userSessionStore.isLoggedIn()) {
                startActivity(new Intent(requireContext(), MyCommentsActivity.class));
            } else {
                promptLoginRequired();
            }
        });
        binding.rowNotificationSettings.setOnClickListener(v -> binding.switchNotifications.performClick());
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationToggleChanged(isChecked));
        binding.rowAppearanceSettings.setOnClickListener(v -> showAppearanceDialog());
        binding.textAppearancePreview.setOnClickListener(v -> showAppearanceDialog());
        loadReminderSettingsFromStore();
        refreshReminderSettings();
    }

    private void render(ProfileData profileData) {
        boolean guestMode = isGuestMode();
        String nameToDisplay = guestMode ? getString(R.string.guest_display_name) : profileData.getName();
        displayedUsername = nameToDisplay;
        binding.textAvatar.setText(guestMode ? "GU" : buildAvatar(profileData.getName()));
        binding.textName.setText(nameToDisplay);
        binding.textEmail.setText(guestMode ? getString(R.string.guest_profile_hint) : profileData.getEmail());
        binding.textCurrentStreak.setText(guestMode ? "--" : String.valueOf(profileData.getCurrentStreak()));
        binding.textLongestStreak.setText(guestMode ? "--" : String.valueOf(profileData.getLongestStreak()));
        binding.textTotalStudy.setText(guestMode ? "--" : profileData.getTotalStudyTime());
        binding.textMood.setText(guestMode
                ? getString(R.string.guest_profile_cta)
                : profileData.getStreakSummary().getActiveMood() + " mood");
        binding.layoutProfileStats.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardWeeklyActivity.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardMood.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardAccountActions.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardGuestProfilePromo.setVisibility(guestMode ? View.VISIBLE : View.GONE);
        binding.textGuestProfileTitle.setText(R.string.guest_profile_title);
        binding.textGuestProfileBody.setText(R.string.guest_profile_body);
        if (!guestMode) {
            renderActivityBars(profileData);
        } else {
            binding.activityChartContainer.removeAllViews();
        }
        renderThemeMode();
        renderSettingsState();
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
        bar.setBackgroundColor(activity.isHighlighted()
                ? ThemeColorResolver.resolveColor(context, R.attr.ttColorPrimary)
                : ThemeColorResolver.resolveColor(context, R.attr.ttColorBorder));
        wrapper.addView(bar);

        TextView label = new TextView(context);
        label.setText(activity.getDayLabel());
        label.setTextColor(ThemeColorResolver.resolveColor(context, R.attr.ttColorTextSecondary));
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
        int activeColor = ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorPrimary);
        int inactiveColor = ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorTextSecondary);
        binding.textThemeLight.setTextColor(darkMode ? inactiveColor : activeColor);
        binding.textThemeDark.setTextColor(darkMode ? activeColor : inactiveColor);
        binding.textThemeLight.setAlpha(darkMode ? 0.7f : 1f);
        binding.textThemeDark.setAlpha(darkMode ? 1f : 0.7f);
    }

    private void renderSettingsState() {
        if (binding == null) {
            return;
        }
        boolean guestMode = isGuestMode();
        boolean notificationsEnabled = NotificationPreferenceStore.isEnabled(requireContext());
        AppPalette palette = AppearancePreferenceStore.getPalette(requireContext());
        binding.textAppearanceSummary.setText(getString(R.string.settings_palette_summary, getString(palette.getLabelResId())));
        binding.textSettingsGuestHint.setVisibility(guestMode ? View.VISIBLE : View.GONE);
        binding.textSettingsGuestHint.setText(R.string.settings_guest_device_hint);
        binding.rowNotificationSettings.setAlpha(1f);
        binding.switchNotifications.setEnabled(true);
        syncingNotificationSwitch = true;
        binding.switchNotifications.setChecked(notificationsEnabled);
        syncingNotificationSwitch = false;
        if (!remoteSettingsLoaded && !remoteSettingsSyncFailed) {
            binding.textNotificationSummary.setText(R.string.settings_notifications_syncing_summary);
            return;
        }
        if (Boolean.FALSE.equals(remoteDailyReminderEnabled)) {
            binding.textNotificationSummary.setText(R.string.settings_notifications_admin_disabled_summary);
            return;
        }
        if (!hasNotificationPermission()) {
            binding.textNotificationSummary.setText(R.string.settings_notifications_permission_required);
            return;
        }
        if (hasRemoteReminderSchedule()) {
            binding.textNotificationSummary.setText(getString(
                    notificationsEnabled
                            ? R.string.settings_notifications_enabled_with_schedule
                            : R.string.settings_notifications_disabled_with_schedule,
                    remoteDailyReminderTime,
                    remoteDailyReminderTimezone
            ));
            return;
        }
        binding.textNotificationSummary.setText(notificationsEnabled
                ? R.string.settings_notifications_ready_summary
                : R.string.settings_notifications_disabled_summary);
    }

    private void onNotificationToggleChanged(boolean isChecked) {
        if (syncingNotificationSwitch) {
            return;
        }
        if (isChecked && !hasNotificationPermission()) {
            syncingNotificationSwitch = true;
            binding.switchNotifications.setChecked(false);
            syncingNotificationSwitch = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPreferenceStore.markPermissionRequested(requireContext());
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return;
        }
        persistNotificationPreference(isChecked);
    }

    private void showAppearanceDialog() {
        DialogAppearanceSettingsBinding dialogBinding = DialogAppearanceSettingsBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.textAppearanceDialogSummary.setText(R.string.settings_palette_dialog_message);

        AppPalette currentPalette = AppearancePreferenceStore.getPalette(requireContext());
        selectPaletteOption(dialogBinding.groupPaletteOptions, currentPalette);
        applyPalettePreview(dialogBinding, currentPalette);
        dialogBinding.groupPaletteOptions.setOnCheckedChangeListener((group, checkedId) ->
                applyPalettePreview(dialogBinding, resolvePaletteFromSelection(checkedId))
        );

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_palette_dialog_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.settings_palette_cancel, null)
                .setPositiveButton(R.string.settings_palette_apply, (dialog, which) -> {
                    AppPalette selectedPalette = resolvePaletteFromSelection(
                            dialogBinding.groupPaletteOptions.getCheckedRadioButtonId()
                    );
                    AppearancePreferenceStore.setPalette(requireContext(), selectedPalette);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.settings_palette_applied, getString(selectedPalette.getLabelResId())),
                            Toast.LENGTH_SHORT
                    ).show();
                    requireActivity().recreate();
                })
                .show();
    }

    private void applyPalettePreview(DialogAppearanceSettingsBinding binding, AppPalette palette) {
        Context previewContext = new ContextThemeWrapper(requireContext(), palette.getThemeOverlayResId());
        int backgroundColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorBackground);
        int surfaceColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorSurface);
        int surfaceAltColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorSurfaceAlt);
        int primaryColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorPrimary);
        int primaryTintColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorPrimaryTint);
        int textPrimaryColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorTextPrimary);
        int textSecondaryColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorTextSecondary);
        int borderColor = ThemeColorResolver.resolveColor(previewContext, R.attr.ttColorBorder);

        binding.cardAppearancePreview.setCardBackgroundColor(surfaceColor);
        binding.cardAppearancePreview.setStrokeColor(borderColor);
        binding.layoutAppearancePreviewRoot.setBackgroundColor(backgroundColor);
        binding.cardPreviewStat.setCardBackgroundColor(surfaceAltColor);
        binding.cardPreviewStat.setStrokeColor(borderColor);
        binding.textPreviewTitle.setTextColor(textPrimaryColor);
        binding.textPreviewBody.setTextColor(textSecondaryColor);
        binding.textPreviewStatLabel.setTextColor(textSecondaryColor);
        binding.textPreviewStatValue.setTextColor(primaryColor);
        binding.buttonPreviewAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
        binding.buttonPreviewAction.setTextColor(ContextCompat.getColor(previewContext, R.color.white));

        GradientDrawable badgeBackground = new GradientDrawable();
        badgeBackground.setColor(primaryTintColor);
        badgeBackground.setCornerRadius(dp(999));
        binding.textPreviewBadge.setBackground(badgeBackground);
        binding.textPreviewBadge.setTextColor(primaryColor);
    }

    private void selectPaletteOption(RadioGroup group, AppPalette palette) {
        int checkedId;
        switch (palette) {
            case OCEAN:
                checkedId = R.id.radioPaletteOcean;
                break;
            case FOREST:
                checkedId = R.id.radioPaletteForest;
                break;
            case SUNSET:
                checkedId = R.id.radioPaletteSunset;
                break;
            case CLASSIC:
            default:
                checkedId = R.id.radioPaletteClassic;
                break;
        }
        group.check(checkedId);
    }

    private AppPalette resolvePaletteFromSelection(int checkedId) {
        if (checkedId == R.id.radioPaletteOcean) {
            return AppPalette.OCEAN;
        }
        if (checkedId == R.id.radioPaletteForest) {
            return AppPalette.FOREST;
        }
        if (checkedId == R.id.radioPaletteSunset) {
            return AppPalette.SUNSET;
        }
        return AppPalette.CLASSIC;
    }

    private boolean hasNotificationPermission() {
        return DailyReminderScheduler.hasNotificationPermission(requireContext());
    }

    private void persistNotificationPreference(boolean enabled) {
        NotificationPreferenceStore.setEnabled(requireContext(), enabled);
        DailyReminderScheduler.apply(requireContext());
        Toast.makeText(
                requireContext(),
                enabled ? R.string.settings_notifications_enabled_toast : R.string.settings_notifications_disabled_toast,
                Toast.LENGTH_SHORT
        ).show();
        renderSettingsState();
    }

    private void refreshReminderSettings() {
        if (mobileApiService == null) {
            return;
        }
        mobileApiService.getReminderSettings().enqueue(new Callback<MobileReminderSettingsDto>() {
            @Override
            public void onResponse(Call<MobileReminderSettingsDto> call, Response<MobileReminderSettingsDto> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) {
                    return;
                }
                applyReminderSettings(response.body());
            }

            @Override
            public void onFailure(Call<MobileReminderSettingsDto> call, Throwable throwable) {
                if (!isAdded() || binding == null) {
                    return;
                }
                remoteSettingsSyncFailed = true;
                loadReminderSettingsFromStore();
                DailyReminderScheduler.apply(requireContext());
                renderSettingsState();
            }
        });
    }

    private void applyReminderSettings(MobileReminderSettingsDto settings) {
        ReminderSettingsStore.save(
                requireContext(),
                settings.dailyReminderEnabled,
                settings.dailyReminderTime,
                settings.dailyReminderTimezone
        );
        loadReminderSettingsFromStore();
        remoteSettingsLoaded = true;
        remoteSettingsSyncFailed = false;
        DailyReminderScheduler.apply(requireContext());
        renderSettingsState();
    }

    private void loadReminderSettingsFromStore() {
        remoteDailyReminderEnabled = ReminderSettingsStore.isDailyReminderEnabled(requireContext());
        remoteDailyReminderTime = ReminderSettingsStore.getDailyReminderTime(requireContext());
        remoteDailyReminderTimezone = ReminderSettingsStore.getDailyReminderTimezone(requireContext());
    }

    private boolean hasRemoteReminderSchedule() {
        return Boolean.TRUE.equals(remoteDailyReminderEnabled)
                && remoteDailyReminderTime != null
                && !remoteDailyReminderTime.trim().isEmpty()
                && remoteDailyReminderTimezone != null
                && !remoteDailyReminderTimezone.trim().isEmpty();
    }

    private void resetRemoteSettingsState() {
        remoteSettingsLoaded = false;
        remoteSettingsSyncFailed = false;
        loadReminderSettingsFromStore();
    }

    private boolean isGuestMode() {
        return userSessionStore == null || !userSessionStore.isLoggedIn();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null || userSessionStore == null) return;
        renderThemeMode();
        resetRemoteSettingsState();
        renderSettingsState();
        refreshReminderSettings();
        DailyReminderScheduler.apply(requireContext());
        if (userSessionStore.isLoggedIn()) {
            String currentUsername = userSessionStore.getUsername();
            if (currentUsername != null && !currentUsername.equals(displayedUsername)) {
                displayedUsername = currentUsername;
                binding.textAvatar.setText(buildAvatar(currentUsername));
                binding.textName.setText(currentUsername);
            }
            renderSessionCard(null);
        } else {
            binding.textSessionStatus.setText(R.string.session_guest_mode);
            binding.buttonSessionAction.setText(R.string.button_sign_in);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
