package hcmute.edu.vn.nguyenthetan.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderScheduler;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileReminderSettingsDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityMainBinding;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.explore.ExploreFragment;
import hcmute.edu.vn.nguyenthetan.ui.home.HomeFragment;
import hcmute.edu.vn.nguyenthetan.ui.leaderboard.LeaderboardFragment;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;
import hcmute.edu.vn.nguyenthetan.ui.lessonlist.LessonListActivity;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;
import hcmute.edu.vn.nguyenthetan.ui.profile.ProfileFragment;

public class MainActivity extends ThemedActivity implements
        HomeFragment.Listener,
        ExploreFragment.Listener,
        ProfileFragment.Listener {

    private ActivityMainBinding binding;
    private boolean serverReady;
    private MobileApiService mobileApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        NetworkUtils.getNetworkLiveData(this).observe(this, isAvailable -> {
            binding.offlineBanner.setVisibility(isAvailable ? View.GONE : View.VISIBLE);
        });

        TungTungApplication application = (TungTungApplication) getApplication();
        mobileApiService = application.getAppContainer().getMobileApiService();
        syncReminderSettings();
        DailyReminderScheduler.apply(this);
        if (!NetworkUtils.isNetworkAvailable(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        final boolean shouldSelectHome = savedInstanceState == null;
        binding.progressSync.setVisibility(View.VISIBLE);
        application.getAppContainer().checkServerAvailability((available, message) -> {
            if (!available) {
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
                return;
            }
            serverReady = true;
            binding.progressSync.setVisibility(View.GONE);
            if (shouldSelectHome) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (!serverReady) {
                return false;
            }
            int itemId = item.getItemId();
            Fragment fragment;
            if (itemId == R.id.navigation_home) {
                fragment = HomeFragment.newInstance();
            } else if (itemId == R.id.navigation_explore) {
                fragment = ExploreFragment.newInstance();
            } else if (itemId == R.id.navigation_leaderboard) {
                fragment = LeaderboardFragment.newInstance();
            } else {
                fragment = ProfileFragment.newInstance();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        });

        application.getAppContainer().getIsSyncing().observe(this, syncing -> {
            binding.progressSync.setVisibility(syncing ? View.VISIBLE : View.GONE);
            binding.fragmentContainer.setEnabled(!syncing);
            if (syncing) {
                binding.layoutSyncError.setVisibility(View.GONE);
            }
        });
        application.getAppContainer().getSyncErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                binding.textSyncError.setText(message);
                binding.layoutSyncError.setVisibility(View.VISIBLE);
            } else {
                binding.layoutSyncError.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onContinueLearning(long lessonId) {
        startActivity(LessonActivity.newIntent(this, lessonId));
    }

    @Override
    public void onOpenCategory(String categoryId) {
        startActivity(LessonListActivity.newIntent(this, categoryId == null ? AppDefaults.DEFAULT_CATEGORY_SLUG : categoryId));
    }

    @Override
    public void onOpenStreakDialog() {
        ProfileFragment.showStreakDialog(getSupportFragmentManager(), false);
    }

    @Override
    public void onOpenSettings() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DailyReminderScheduler.apply(this);
    }

    private void syncReminderSettings() {
        if (mobileApiService == null) {
            return;
        }
        mobileApiService.getReminderSettings().enqueue(new retrofit2.Callback<MobileReminderSettingsDto>() {
            @Override
            public void onResponse(retrofit2.Call<MobileReminderSettingsDto> call, retrofit2.Response<MobileReminderSettingsDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    DailyReminderScheduler.apply(MainActivity.this);
                    return;
                }
                MobileReminderSettingsDto body = response.body();
                ReminderSettingsStore.save(
                        MainActivity.this,
                        body.dailyReminderEnabled,
                        body.dailyReminderTime,
                        body.dailyReminderTimezone
                );
                DailyReminderScheduler.apply(MainActivity.this);
            }

            @Override
            public void onFailure(retrofit2.Call<MobileReminderSettingsDto> call, Throwable throwable) {
                DailyReminderScheduler.apply(MainActivity.this);
            }
        });
    }
}

