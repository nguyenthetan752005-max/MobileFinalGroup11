package hcmute.edu.vn.nguyenthetan.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.View;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityMainBinding;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.explore.ExploreFragment;
import hcmute.edu.vn.nguyenthetan.ui.home.HomeFragment;
import hcmute.edu.vn.nguyenthetan.ui.leaderboard.LeaderboardFragment;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;
import hcmute.edu.vn.nguyenthetan.ui.lessonlist.LessonListActivity;
import hcmute.edu.vn.nguyenthetan.ui.profile.ProfileFragment;

public class MainActivity extends ThemedActivity implements
        HomeFragment.Listener,
        ExploreFragment.Listener,
        ProfileFragment.Listener {

    public static final String EXTRA_START_TAB = "extra_start_tab";
    public static final String TAB_HOME = "HOME";
    public static final String TAB_EXPLORE = "EXPLORE";

    private ActivityMainBinding binding;

    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private LeaderboardFragment leaderboardFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

    public static Intent newIntent(Context context, String startTab) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_START_TAB, startTab);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HOME");
            exploreFragment = (ExploreFragment) getSupportFragmentManager().findFragmentByTag("EXPLORE");
            leaderboardFragment = (LeaderboardFragment) getSupportFragmentManager().findFragmentByTag("LEADERBOARD");
            profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE");
            if (homeFragment == null) homeFragment = HomeFragment.newInstance();
            if (exploreFragment == null) exploreFragment = ExploreFragment.newInstance();
            if (leaderboardFragment == null) leaderboardFragment = LeaderboardFragment.newInstance();
            if (profileFragment == null) profileFragment = ProfileFragment.newInstance();
            
            String activeTab = savedInstanceState.getString("ACTIVE_TAB");
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f != null && f.getTag() != null && f.getTag().equals(activeTab)) {
                    activeFragment = f;
                }
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment;
            String tag;

            if (itemId == R.id.navigation_home) {
                if (homeFragment == null) homeFragment = HomeFragment.newInstance();
                targetFragment = homeFragment;
                tag = "HOME";
            } else if (itemId == R.id.navigation_explore) {
                if (exploreFragment == null) exploreFragment = ExploreFragment.newInstance();
                targetFragment = exploreFragment;
                tag = "EXPLORE";
            } else if (itemId == R.id.navigation_leaderboard) {
                if (leaderboardFragment == null) leaderboardFragment = LeaderboardFragment.newInstance();
                targetFragment = leaderboardFragment;
                tag = "LEADERBOARD";
            } else {
                if (profileFragment == null) profileFragment = ProfileFragment.newInstance();
                targetFragment = profileFragment;
                tag = "PROFILE";
            }

            if (targetFragment == activeFragment) {
                return true;
            }

            androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (activeFragment != null) {
                transaction.hide(activeFragment);
            }
            if (targetFragment.isAdded()) {
                transaction.show(targetFragment);
            } else {
                transaction.add(R.id.fragmentContainer, targetFragment, tag);
            }
            transaction.commit();

            activeFragment = targetFragment;
            return true;
        });

        if (savedInstanceState == null) {
            String requestedTab = getIntent() != null ? getIntent().getStringExtra(EXTRA_START_TAB) : null;
            int startItemId = TAB_EXPLORE.equals(requestedTab)
                    ? R.id.navigation_explore
                    : R.id.navigation_home;
            binding.bottomNavigation.setSelectedItemId(startItemId);
        } else if (activeFragment != null && activeFragment.isAdded()) {
            String tag = activeFragment.getTag();
            if ("HOME".equals(tag)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
            } else if ("EXPLORE".equals(tag)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_explore);
            } else if ("LEADERBOARD".equals(tag)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_leaderboard);
            } else {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_profile);
            }
        }

        TungTungApplication application = (TungTungApplication) getApplication();
        hcmute.edu.vn.nguyenthetan.core.AppPalette initialPalette = hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore.getPalette(this);
        application.getAppContainer().getIsSyncing().observe(this, syncing -> {
            boolean syncingNow = Boolean.TRUE.equals(syncing);
            binding.progressSync.setVisibility(syncingNow ? View.VISIBLE : View.GONE);
            binding.fragmentContainer.animate().alpha(syncingNow ? 0.3f : 1.0f).setDuration(200);
            if (!syncingNow && hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore.getPalette(this) != initialPalette) {
                recreate();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent == null) return;
        String requestedTab = intent.getStringExtra(EXTRA_START_TAB);
        if (TAB_EXPLORE.equals(requestedTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_explore);
        } else if (TAB_HOME.equals(requestedTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeFragment != null && activeFragment.getTag() != null) {
            outState.putString("ACTIVE_TAB", activeFragment.getTag());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String activeTab = savedInstanceState.getString("ACTIVE_TAB");
        if (activeTab != null) {
            if ("HOME".equals(activeTab)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
            } else if ("EXPLORE".equals(activeTab)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_explore);
            } else if ("LEADERBOARD".equals(activeTab)) {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_leaderboard);
            } else {
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_profile);
            }
        }
    }

    @Override
    public void onContinueLearning(long lessonId) {
        startActivity(LessonActivity.newIntent(this, lessonId));
    }

    @Override
    public void onOpenCategory(String categoryId) {
        startActivity(LessonListActivity.newIntent(this, categoryId == null ? AppDefaults.DEFAULT_CATEGORY_SLUG : categoryId));
    }
}
