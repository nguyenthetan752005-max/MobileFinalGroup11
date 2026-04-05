package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentLeaderboardBinding;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;

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
        LeaderboardViewModelFactory factory = new LeaderboardViewModelFactory(application.getAppContainer().getLeaderboardUseCase());
        LeaderboardViewModel viewModel = new ViewModelProvider(this, factory).get(LeaderboardViewModel.class);
        viewModel.getLeaderboardState().observe(getViewLifecycleOwner(), data -> {
            if (binding.tabLayout.getSelectedTabPosition() == 1) {
                adapter.submitList(data.getMonthlyEntries());
            } else {
                adapter.submitList(data.getWeeklyEntries());
            }
            binding.textYourRank.setText("#" + data.getCurrentUserRank() + " | " + data.getCurrentUserTime());
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (viewModel.getLeaderboardState().getValue() == null) {
                    return;
                }
                if (tab != null && tab.getPosition() == 1) {
                    adapter.submitList(viewModel.getLeaderboardState().getValue().getMonthlyEntries());
                } else {
                    adapter.submitList(viewModel.getLeaderboardState().getValue().getWeeklyEntries());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
