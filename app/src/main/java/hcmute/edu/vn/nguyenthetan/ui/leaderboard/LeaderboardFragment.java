package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import android.os.Bundle;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentLeaderboardBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private LeaderboardViewModel viewModel;
    private UserSessionStore userSessionStore;
    private Boolean lastSyncing;

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LeaderboardAdapter();
        binding.recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerLeaderboard.setAdapter(adapter);

        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();
        LeaderboardViewModelFactory factory = new LeaderboardViewModelFactory(
                application.getAppContainer().getLeaderboardUseCase(),
                application.getAppContainer().getSyncLeaderboardUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(LeaderboardViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> renderState(state, viewModel));

        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (Boolean.TRUE.equals(lastSyncing) && !Boolean.TRUE.equals(syncing) && viewModel != null) {
                viewModel.forceLoad();
            }
            lastSyncing = syncing;
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LeaderboardUiState state = viewModel.getUiState().getValue();
                if (state == null || state.getStatus() != LeaderboardUiState.Status.SUCCESS || state.getData() == null) {
                    return;
                }
                LeaderboardData data = state.getData();
                if (tab != null && tab.getPosition() == 1) {
                    adapter.submitList(data.getMonthlyEntries());
                } else {
                    adapter.submitList(data.getWeeklyEntries());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refresh();
        });

        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastSyncing = null;
        binding = null;
    }

    private void renderState(LeaderboardUiState state, LeaderboardViewModel viewModel) {
        if (state == null) {
            return;
        }
        switch (state.getStatus()) {
            case LOADING:
                binding.tabLayout.setVisibility(View.GONE);
                binding.recyclerLeaderboard.setVisibility(View.GONE);
                binding.cardYourRank.setVisibility(View.GONE);
                binding.textLeaderboardEmpty.setVisibility(View.VISIBLE);
                binding.textLeaderboardEmpty.setText(getString(R.string.loading));
                break;
            case EMPTY:
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.tabLayout.setVisibility(View.GONE);
                binding.recyclerLeaderboard.setVisibility(View.GONE);
                binding.cardYourRank.setVisibility(View.GONE);
                binding.textLeaderboardEmpty.setVisibility(View.VISIBLE);
                binding.textLeaderboardEmpty.setText(getString(R.string.leaderboard_empty));
                adapter.submitList(Collections.emptyList());
                break;
            case ERROR:
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.tabLayout.setVisibility(View.GONE);
                binding.recyclerLeaderboard.setVisibility(View.GONE);
                binding.cardYourRank.setVisibility(View.GONE);
                binding.textLeaderboardEmpty.setVisibility(View.VISIBLE);
                binding.textLeaderboardEmpty.setText(state.getMessage() == null || state.getMessage().trim().isEmpty()
                        ? getString(R.string.leaderboard_empty)
                        : state.getMessage());
                break;
            case SUCCESS:
                binding.swipeRefreshLayout.setRefreshing(false);
                LeaderboardData data = state.getData();
                if (data == null) {
                    break;
                }
                boolean hasWeekly = data.getWeeklyEntries() != null && !data.getWeeklyEntries().isEmpty();
                boolean hasMonthly = data.getMonthlyEntries() != null && !data.getMonthlyEntries().isEmpty();
                if (binding.tabLayout.getSelectedTabPosition() == 1) {
                    adapter.submitList(hasMonthly ? data.getMonthlyEntries() : Collections.emptyList());
                } else {
                    adapter.submitList(hasWeekly ? data.getWeeklyEntries() : Collections.emptyList());
                }
                binding.textActiveTime.setText(getString(R.string.leaderboard_active_time, data.getCurrentUserTime()));
                binding.textYourRank.setText(data.getCurrentUserRank() > 0
                        ? "#" + data.getCurrentUserRank() + " | " + data.getCurrentUserTime()
                        : data.getCurrentUserTime());
                boolean isGuest = userSessionStore == null || !userSessionStore.isLoggedIn();
                binding.tabLayout.setVisibility(View.VISIBLE);
                binding.recyclerLeaderboard.setVisibility(View.VISIBLE);
                binding.cardYourRank.setVisibility(isGuest ? View.GONE : View.VISIBLE);
                binding.textLeaderboardEmpty.setVisibility(View.GONE);
                break;
            case IDLE:
            default:
                if (viewModel != null) {
                    viewModel.load();
                }
                break;
        }
    }
}
