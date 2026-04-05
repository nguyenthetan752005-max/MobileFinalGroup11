package hcmute.edu.vn.nguyenthetan;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.View;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityMainBinding;
import hcmute.edu.vn.nguyenthetan.ui.explore.ExploreFragment;
import hcmute.edu.vn.nguyenthetan.ui.home.HomeFragment;
import hcmute.edu.vn.nguyenthetan.ui.leaderboard.LeaderboardFragment;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;
import hcmute.edu.vn.nguyenthetan.ui.lessonlist.LessonListActivity;
import hcmute.edu.vn.nguyenthetan.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.Listener,
        ExploreFragment.Listener,
        ProfileFragment.Listener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
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

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

        TungTungApplication application = (TungTungApplication) getApplication();
        application.getAppContainer().getIsSyncing().observe(this, syncing -> {
            binding.progressSync.setVisibility(syncing ? View.VISIBLE : View.GONE);
            binding.fragmentContainer.animate().alpha(syncing ? 0.3f : 1.0f).setDuration(200);
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
}
