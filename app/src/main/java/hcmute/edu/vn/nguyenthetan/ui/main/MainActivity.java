package hcmute.edu.vn.nguyenthetan.ui.main;

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

    private ActivityMainBinding binding;

    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private LeaderboardFragment leaderboardFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;

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
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f != null && f.isVisible()) {
                    activeFragment = f;
                    break;
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

            if (targetFragment.isAdded()) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment != null ? activeFragment : targetFragment)
                        .show(targetFragment)
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentContainer, targetFragment, tag)
                        .hide(activeFragment != null ? activeFragment : targetFragment)
                        .show(targetFragment)
                        .commit();
            }

            activeFragment = targetFragment;
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
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
    public void onContinueLearning(long lessonId) {
        startActivity(LessonActivity.newIntent(this, lessonId));
    }

    @Override
    public void onOpenCategory(String categoryId) {
        startActivity(LessonListActivity.newIntent(this, categoryId == null ? AppDefaults.DEFAULT_CATEGORY_SLUG : categoryId));
    }
}
