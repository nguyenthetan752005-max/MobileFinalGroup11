package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.TimeZone;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppPalette;
import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderNotificationHelper;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderScheduler;
import hcmute.edu.vn.nguyenthetan.core.MascotMoodResolver;
import hcmute.edu.vn.nguyenthetan.core.NotificationPreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.core.ThemePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ReminderSettings;
import hcmute.edu.vn.nguyenthetan.databinding.DialogAppearanceSettingsBinding;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentProfileBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.DailyActivity;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;
import hcmute.edu.vn.nguyenthetan.ui.common.StreakDialogFragment;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface Listener {
    }

    private FragmentProfileBinding binding;
    private UserSessionStore userSessionStore;
    private ProfileViewModel viewModel;
    private Boolean lastSyncing;
    private boolean syncingNotificationSwitch;
    private boolean remoteSettingsLoaded;
    private boolean remoteSettingsSyncFailed;
    private Boolean remoteDailyReminderEnabled;
    private String remoteDailyReminderTime;
    private String remoteDailyReminderTimezone;
    private Uri cameraPhotoUri;
    private boolean sendTestReminderAfterPermissionGrant;
    private boolean avatarUploadInProgress;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!isAdded() || binding == null) {
                    return;
                }
                boolean shouldSendTestReminder = sendTestReminderAfterPermissionGrant;
                sendTestReminderAfterPermissionGrant = false;
                NotificationPreferenceStore.markPermissionRequested(requireContext());
                if (granted) {
                    NotificationPreferenceStore.setEnabled(requireContext(), true);
                    DailyReminderScheduler.apply(requireContext());
                    if (shouldSendTestReminder) {
                        sendTestReminderNow();
                    } else {
                        Toast.makeText(requireContext(), R.string.settings_notifications_enabled_toast, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    NotificationPreferenceStore.setEnabled(requireContext(), false);
                    DailyReminderScheduler.cancel(requireContext());
                    Toast.makeText(requireContext(), R.string.settings_notifications_permission_denied, Toast.LENGTH_LONG).show();
                }
                renderSettingsState();
            });
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!isAdded() || binding == null) return;
                if (granted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), R.string.avatar_camera_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded() || binding == null) return;
                revokeUriPermissionQuietly(cameraPhotoUri);
                if (result.getResultCode() == android.app.Activity.RESULT_OK && cameraPhotoUri != null) {
                    launchAvatarCrop(cameraPhotoUri);
                }
            });
    private final ActivityResultLauncher<Intent> cropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded() || binding == null) return;
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        handleAvatarResult(croppedUri);
                    }
                    return;
                }
                if (result.getData() != null && UCrop.getError(result.getData()) != null) {
                    Log.e(TAG, "Avatar crop failed", UCrop.getError(result.getData()));
                    Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded() || binding == null) return;
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    try {
                        Uri selectedUri = result.getData().getData();
                        Uri internalUri = copyGalleryImageToCache(selectedUri);
                        launchAvatarCrop(internalUri != null ? internalUri : selectedUri);
                    } catch (Exception e) {
                        Log.e(TAG, "Avatar gallery import failed", e);
                        Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
                    }
                }
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
        ProfileViewModelFactory factory = new ProfileViewModelFactory(
                application.getAppContainer().getProfileUseCase(),
                application.getAppContainer().getReminderSettingsUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        viewModel.getProfileState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), loading ->
                binding.progressProfileLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        viewModel.getReminderState().observe(getViewLifecycleOwner(), this::onReminderResult);
        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (Boolean.TRUE.equals(lastSyncing) && !Boolean.TRUE.equals(syncing) && viewModel != null) {
                viewModel.forceLoad();
            }
            lastSyncing = syncing;
        });
        viewModel.load();

        binding.textThemeLight.setOnClickListener(v -> {
            ThemePreferenceStore.setThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.textThemeDark.setOnClickListener(v -> {
            ThemePreferenceStore.setThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_YES);
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

        binding.buttonChangeAvatar.setOnClickListener(v -> showAvatarPickerDialog());
        loadSavedAvatar();

        binding.rowNotificationSettings.setOnClickListener(v -> binding.switchNotifications.performClick());
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationToggleChanged(isChecked));
        binding.buttonReminderTime.setOnClickListener(v -> showReminderTimePicker());
        binding.buttonNotificationTest.setOnClickListener(v -> sendTestReminder());
        binding.rowAppearanceSettings.setOnClickListener(v -> showAppearanceDialog());
        binding.textAppearancePreview.setOnClickListener(v -> showAppearanceDialog());
        loadReminderSettingsFromStore();
        refreshReminderSettings();
    }

    private void render(ProfileData profileData) {
        if (profileData == null) return;
        boolean guestMode = isGuestMode();
        String nameToDisplay = guestMode ? getString(R.string.guest_display_name) : profileData.getName();
        binding.textAvatar.setText(guestMode ? "GU" : buildAvatar(profileData.getName()));
        renderAvatar(profileData);
        binding.textName.setText(nameToDisplay);
        binding.textEmail.setText(guestMode ? getString(R.string.guest_profile_hint) : profileData.getEmail());
        binding.textCurrentStreak.setText(guestMode ? "--" : String.valueOf(profileData.getCurrentStreak()));
        binding.textLongestStreak.setText(guestMode ? "--" : String.valueOf(profileData.getLongestStreak()));
        binding.textTotalStudy.setText(guestMode ? "--" : profileData.getTotalStudyTime());
        binding.layoutProfileStats.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardWeeklyActivity.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.cardMood.setVisibility(View.GONE);
        binding.cardAccountActions.setVisibility(guestMode ? View.GONE : View.VISIBLE);
        binding.buttonChangeAvatar.setVisibility(guestMode ? View.GONE : View.VISIBLE);
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
        renderSessionCard();
    }

    private void renderSessionCard() {
        if (userSessionStore != null && userSessionStore.isLoggedIn()) {
            binding.textSessionStatus.setVisibility(View.VISIBLE);
            binding.textSessionStatus.setText(getString(R.string.session_logged_in, userSessionStore.getUsername()));
            binding.buttonSessionAction.setText(R.string.button_logout);
        } else {
            binding.textSessionStatus.setVisibility(View.VISIBLE);
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
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.US);
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
        boolean hasLocalTimeOverride = ReminderSettingsStore.hasLocalTimeOverride(requireContext());
        String reminderTime = remoteDailyReminderTime == null
                ? ReminderSettingsStore.getDailyReminderTime(requireContext())
                : remoteDailyReminderTime;
        String reminderTimezone = remoteDailyReminderTimezone == null
                ? ReminderSettingsStore.getDailyReminderTimezone(requireContext())
                : remoteDailyReminderTimezone;
        AppPalette palette = AppearancePreferenceStore.getPalette(requireContext());
        if (AppearancePreferenceStore.isScaryMoodActive(requireContext())) {
            binding.textAppearanceSummary.setText(R.string.settings_palette_scary_summary);
        } else {
            binding.textAppearanceSummary.setText(getString(R.string.settings_palette_summary, getString(palette.getLabelResId())));
        }
        binding.textReminderTimeValue.setText(getString(
                hasLocalTimeOverride
                        ? R.string.settings_reminder_time_value_device_override
                        : R.string.settings_reminder_time_value,
                reminderTime,
                formatReminderTimezone(reminderTimezone)
        ));
        renderReminderPreview();
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
                            ? (hasLocalTimeOverride
                            ? R.string.settings_notifications_enabled_with_device_schedule
                            : R.string.settings_notifications_enabled_with_schedule)
                            : (hasLocalTimeOverride
                            ? R.string.settings_notifications_disabled_with_device_schedule
                            : R.string.settings_notifications_disabled_with_schedule),
                    reminderTime,
                    formatReminderTimezone(reminderTimezone)
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

    private void showReminderTimePicker() {
        String reminderTime = remoteDailyReminderTime == null
                ? ReminderSettingsStore.getDailyReminderTime(requireContext())
                : remoteDailyReminderTime;
        int[] parsedTime = parseReminderTime(reminderTime);
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    ReminderSettingsStore.saveLocalTimeOverride(
                            requireContext(),
                            selectedTime,
                            TimeZone.getDefault().getID()
                    );
                    loadReminderSettingsFromStore();
                    DailyReminderScheduler.apply(requireContext());
                    renderSettingsState();
                    Toast.makeText(requireContext(), R.string.settings_notification_time_saved, Toast.LENGTH_SHORT).show();
                },
                parsedTime[0],
                parsedTime[1],
                DateFormat.is24HourFormat(requireContext())
        );
        timePickerDialog.show();
    }

    private void sendTestReminder() {
        if (!hasNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sendTestReminderAfterPermissionGrant = true;
                NotificationPreferenceStore.markPermissionRequested(requireContext());
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return;
        }
        sendTestReminderNow();
    }

    private void showAppearanceDialog() {
        if (AppearancePreferenceStore.isScaryMoodActive(requireContext())) {
            Toast.makeText(requireContext(), R.string.scary_palette_locked, Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (viewModel != null) {
            viewModel.refreshReminderSettings();
        }
    }

    private void onReminderResult(ProfileViewModel.ReminderResult result) {
        if (!isAdded() || binding == null || result == null) return;
        if (result.fetched && result.settings != null) {
            applyReminderSettings(result.settings);
            return;
        }
        remoteSettingsSyncFailed = true;
        loadReminderSettingsFromStore();
        DailyReminderScheduler.apply(requireContext());
        renderSettingsState();
    }

    private void applyReminderSettings(ReminderSettings settings) {
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

    private void renderReminderPreview() {
        MascotMoodResolver.Mood mood = DailyReminderNotificationHelper.resolveCurrentMood(requireContext());
        binding.imageReminderPreviewMascot.setImageResource(mood.getDrawableRes());
        binding.textReminderPreviewTitle.setText(getString(MascotMoodResolver.getReminderTitleRes(mood)));
        binding.textReminderPreviewBody.setText(getString(MascotMoodResolver.getReminderBodyRes(mood)));
    }

    private void sendTestReminderNow() {
        new Thread(() -> {
            DailyReminderNotificationHelper.showReminderNotification(requireContext());
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), R.string.settings_notification_test_sent, Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private int[] parseReminderTime(String reminderTime) {
        int hour = 19;
        int minute = 0;
        if (reminderTime != null && reminderTime.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            String[] parts = reminderTime.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                hour = 19;
                minute = 0;
            }
        }
        return new int[]{hour, minute};
    }

    private String formatReminderTimezone(String reminderTimezone) {
        if (reminderTimezone == null || reminderTimezone.trim().isEmpty()) {
            return TimeZone.getDefault().getID().replace('_', ' ');
        }
        return reminderTimezone.trim().replace('_', ' ');
    }

    private boolean isGuestMode() {
        return userSessionStore == null || !userSessionStore.isLoggedIn();
    }

    // ── Avatar helpers ──────────────────────────────────────────────

    private void showAvatarPickerDialog() {
        String[] options = {
                getString(R.string.avatar_option_camera),
                getString(R.string.avatar_option_gallery)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.avatar_picker_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        try {
            File photoFile = new File(requireContext().getFilesDir(), getCameraTempFileName());
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            for (ResolveInfo resolveInfo : requireContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                requireContext().grantUriPermission(
                        resolveInfo.activityInfo.packageName,
                        cameraPhotoUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                );
            }
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.avatar_camera_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryLauncher.launch(intent);
    }

    private void launchAvatarCrop(Uri sourceUri) {
        try {
            File destFile = new File(requireContext().getCacheDir(), getCroppedAvatarFileName());
            Uri destUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    destFile
            );
            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            options.setShowCropFrame(false);
            options.setShowCropGrid(false);
            Intent intent = UCrop.of(sourceUri, destUri)
                    .withOptions(options)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(512, 512)
                    .getIntent(requireContext());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cropLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Launch avatar crop failed", e);
            Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAvatarResult(Uri sourceUri) {
        try {
            Bitmap bitmap;
            if (sourceUri.getScheme() != null && sourceUri.getScheme().equals("content")) {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();
            } else {
                bitmap = BitmapFactory.decodeFile(sourceUri.getPath());
            }
            if (bitmap == null) {
                Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            bitmap = compressBitmap(bitmap, 800);
            uploadAvatar(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Handle avatar result failed", e);
            Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAvatar(Bitmap bitmap) {
        if (avatarUploadInProgress) {
            return;
        }
        if (userSessionStore == null || !userSessionStore.isLoggedIn()) {
            promptLoginRequired();
            return;
        }
        long userId = userSessionStore.getUserId();
        if (userId <= 0L) {
            Toast.makeText(requireContext(), R.string.avatar_load_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        setAvatarUploadInProgress(true);
        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        new Thread(() -> {
            String errorMessage = null;
            boolean uploadSucceeded = false;
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                RequestBody avatarRequest = RequestBody.create(
                        MediaType.parse("image/jpeg"),
                        outputStream.toByteArray()
                );
                MultipartBody.Part avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        getAvatarUploadFileName(userId),
                        avatarRequest
                );
                Response<GenericApiResponseDto> response = application.getAppContainer()
                        .getMobileApiService()
                        .updateAvatar(userId, avatarPart)
                        .execute();
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    Log.e(TAG, "Avatar upload failed. HTTP " + response.code());
                    errorMessage = extractAvatarUploadError(response);
                    if (errorMessage != null) {
                        Log.e(TAG, "Avatar upload error body: " + errorMessage);
                    }
                } else {
                    saveAvatarImage(bitmap);
                    application.getAppContainer().refreshCurrentUserProfile();
                    uploadSucceeded = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Avatar upload request failed", e);
                errorMessage = e.getMessage();
            }

            if (!uploadSucceeded && (errorMessage == null || errorMessage.trim().isEmpty())) {
                errorMessage = getString(R.string.avatar_load_failed);
            }
            String finalErrorMessage = normalizeAvatarErrorMessage(errorMessage);
            if (!isAdded()) {
                avatarUploadInProgress = false;
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded() || binding == null) {
                    avatarUploadInProgress = false;
                    return;
                }
                setAvatarUploadInProgress(false);
                if (finalErrorMessage != null) {
                    Toast.makeText(requireContext(), finalErrorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.imageAvatar.setImageBitmap(bitmap);
                binding.imageAvatar.setVisibility(View.VISIBLE);
                binding.textAvatar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.avatar_saved, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void saveAvatarImage(Bitmap bitmap) {
        try {
            File file = getAvatarFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException ignored) {
        }
    }

    private void loadSavedAvatar() {
        File file = getAvatarFile();
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                binding.imageAvatar.setImageBitmap(bitmap);
                binding.imageAvatar.setVisibility(View.VISIBLE);
                binding.textAvatar.setVisibility(View.GONE);
                return;
            }
        }
        binding.imageAvatar.setVisibility(View.GONE);
        binding.textAvatar.setVisibility(View.VISIBLE);
    }

    private void renderAvatar(ProfileData profileData) {
        if (binding == null || profileData == null) {
            return;
        }
        String avatarUrl = profileData.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            binding.imageAvatar.setVisibility(View.VISIBLE);
            binding.textAvatar.setVisibility(View.GONE);
            Glide.with(this)
                    .load(avatarUrl.trim())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .error(Glide.with(this).load(getAvatarFile()).circleCrop())
                    .into(binding.imageAvatar);
            return;
        }
        loadSavedAvatar();
    }

    private File getAvatarFile() {
        long userId = userSessionStore == null ? -1L : userSessionStore.getUserId();
        return new File(requireContext().getFilesDir(), getAvatarUploadFileName(userId));
    }

    private String getAvatarUploadFileName(long userId) {
        if (userId > 0L) {
            return "user_avatar_" + userId + ".jpg";
        }
        return "user_avatar_guest.jpg";
    }

    private String getCameraTempFileName() {
        long userId = userSessionStore == null ? -1L : userSessionStore.getUserId();
        if (userId > 0L) {
            return "avatar_temp_" + userId + ".jpg";
        }
        return "avatar_temp.jpg";
    }

    private String getCroppedAvatarFileName() {
        long userId = userSessionStore == null ? -1L : userSessionStore.getUserId();
        if (userId > 0L) {
            return "avatar_cropped_" + userId + ".jpg";
        }
        return "avatar_cropped.jpg";
    }

    private String getGalleryImportFileName(String extension) {
        String safeExtension = (extension == null || extension.trim().isEmpty()) ? "jpg" : extension.trim().toLowerCase(Locale.US);
        long userId = userSessionStore == null ? -1L : userSessionStore.getUserId();
        if (userId > 0L) {
            return "avatar_gallery_" + userId + "." + safeExtension;
        }
        return "avatar_gallery." + safeExtension;
    }

    private String resolveGalleryFileExtension(Uri sourceUri) {
        if (sourceUri == null) {
            return "jpg";
        }
        String mimeType = requireContext().getContentResolver().getType(sourceUri);
        if (mimeType != null) {
            String fromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (fromMime != null && !fromMime.trim().isEmpty()) {
                return fromMime;
            }
        }
        String path = sourceUri.getLastPathSegment();
        if (path != null) {
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < path.length() - 1) {
                return path.substring(dotIndex + 1).toLowerCase(Locale.US);
            }
        }
        return "jpg";
    }

    private Uri copyGalleryImageToCache(Uri sourceUri) throws IOException {
        if (sourceUri == null) {
            return null;
        }
        File targetFile = new File(requireContext().getCacheDir(), getGalleryImportFileName(resolveGalleryFileExtension(sourceUri)));
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(targetFile, false)) {
            if (inputStream == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                targetFile
        );
    }

    private void revokeUriPermissionQuietly(Uri uri) {
        if (uri == null || !isAdded()) {
            return;
        }
        try {
            requireContext().revokeUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }
    }

    private void setAvatarUploadInProgress(boolean inProgress) {
        avatarUploadInProgress = inProgress;
        if (binding == null) {
            return;
        }
        binding.buttonChangeAvatar.setEnabled(!inProgress);
        binding.buttonChangeAvatar.setAlpha(inProgress ? 0.5f : 1f);
    }

    private String extractAvatarUploadError(Response<GenericApiResponseDto> response) {
        if (response == null) {
            return null;
        }
        GenericApiResponseDto body = response.body();
        if (body != null && body.message != null && !body.message.trim().isEmpty()) {
            return body.message.trim();
        }
        try {
            if (response.errorBody() != null) {
                String rawError = response.errorBody().string();
                if (rawError != null && !rawError.trim().isEmpty()) {
                    return rawError.trim();
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private String normalizeAvatarErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return null;
        }
        String normalized = errorMessage.trim();
        if (normalized.startsWith("{")) {
            return getString(R.string.avatar_load_failed);
        }
        return normalized;
    }

    private Bitmap compressBitmap(Bitmap source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= maxSize && height <= maxSize) return source;
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
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
        if (viewModel != null) {
            if (viewModel.getProfileState().getValue() == null) {
                viewModel.load();
            } else {
                viewModel.forceLoad();
            }
        } else {
            renderSessionCard();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastSyncing = null;
        sendTestReminderAfterPermissionGrant = false;
        avatarUploadInProgress = false;
        binding = null;
    }
}
