package hcmute.edu.vn.nguyenthetan.ui.explore;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentExploreBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;

public class ExploreFragment extends Fragment implements CategoryAdapter.Listener {

    public interface Listener {
        void onOpenCategory(String categoryId);
    }

    private FragmentExploreBinding binding;
    private Listener listener;
    private ExploreViewModel viewModel;
    private TungTungApplication application;
    private Boolean lastSyncing;
    private boolean lastCategoriesEmpty = true;
    private boolean lastLoading;
    private String latestSyncErrorMessage;

    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CategoryAdapter adapter = new CategoryAdapter(this);
        binding.recyclerCategories.setLayoutManager(new GridLayoutManager(requireContext(), resolveSpanCount()));
        binding.recyclerCategories.setAdapter(adapter);

        application = (TungTungApplication) requireActivity().getApplication();
        ExploreViewModelFactory factory = new ExploreViewModelFactory(application.getAppContainer().getExploreCatalogUseCase());
        viewModel = new ViewModelProvider(this, factory).get(ExploreViewModel.class);

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipListening.setOnCheckedChangeListener((buttonView, isChecked) ->
                handlePracticeChipChanged(binding.chipListening, binding.chipSpeaking, viewModel)
        );
        binding.chipSpeaking.setOnCheckedChangeListener((buttonView, isChecked) ->
                handlePracticeChipChanged(binding.chipSpeaking, binding.chipListening, viewModel)
        );
        binding.buttonExploreRetry.setOnClickListener(v -> retryExploreSync());
        updateChipAppearance(binding.chipListening);
        updateChipAppearance(binding.chipSpeaking);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.submitList(categories);
            lastCategoriesEmpty = categories == null || categories.isEmpty();
            renderExploreState();
        });
        viewModel.getLoadingState().observe(getViewLifecycleOwner(), loading -> {
            lastLoading = Boolean.TRUE.equals(loading);
            binding.progressExploreLoad.setVisibility(lastLoading ? View.VISIBLE : View.GONE);
            renderExploreState();
        });
        application.getAppContainer().getSyncErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                latestSyncErrorMessage = message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            } else {
                latestSyncErrorMessage = null;
            }
            renderExploreState();
        });

        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (Boolean.TRUE.equals(lastSyncing) && !Boolean.TRUE.equals(syncing) && viewModel != null) {
                viewModel.forceLoad();
            }
            lastSyncing = syncing;
            renderExploreState();
        });

        viewModel.load();
    }

    private void handlePracticeChipChanged(Chip changedChip, Chip otherChip, ExploreViewModel viewModel) {
        if (!changedChip.isChecked() && !otherChip.isChecked()) {
            changedChip.setChecked(true);
            return;
        }
        updateChipAppearance(binding.chipListening);
        updateChipAppearance(binding.chipSpeaking);
        viewModel.setPracticeFilters(binding.chipListening.isChecked(), binding.chipSpeaking.isChecked());
    }

    private void updateChipAppearance(Chip chip) {
        int backgroundColor = chip.isChecked()
                ? ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorPrimaryTint)
                : ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorSurface);
        int textColor = chip.isChecked()
                ? ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorPrimary)
                : ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorTextSecondary);
        chip.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
        chip.setTextColor(textColor);
        chip.setChipStrokeWidth(chip.isChecked() ? 0f : getResources().getDisplayMetrics().density);
        chip.setChipStrokeColor(ColorStateList.valueOf(
                ThemeColorResolver.resolveColor(requireContext(), R.attr.ttColorBorder)
        ));
    }

    private int resolveSpanCount() {
        Configuration configuration = getResources().getConfiguration();
        boolean tablet = configuration.smallestScreenWidthDp >= 600;
        boolean landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (tablet) {
            return landscape ? 3 : 2;
        }
        return landscape ? 2 : 1;
    }

    @Override
    public void onCategorySelected(ExploreCategory category) {
        if (listener != null) {
            listener.onOpenCategory(category.getId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.forceLoad();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastSyncing = null;
        binding = null;
    }

    private void retryExploreSync() {
        latestSyncErrorMessage = null;
        renderExploreState();
        application.getAppContainer().sync();
    }

    private void renderExploreState() {
        if (binding == null) {
            return;
        }
        boolean syncing = Boolean.TRUE.equals(lastSyncing);
        boolean hasSearchQuery = binding.inputSearch.getText() != null
                && !binding.inputSearch.getText().toString().trim().isEmpty();
        boolean shouldOfferRetry = lastCategoriesEmpty && !hasSearchQuery;
        boolean showRetryState = lastCategoriesEmpty
                && !lastLoading
                && !syncing
                && (shouldOfferRetry || (latestSyncErrorMessage != null && !latestSyncErrorMessage.trim().isEmpty()));
        binding.layoutExploreError.setVisibility(showRetryState ? View.VISIBLE : View.GONE);
        binding.textExploreError.setText(showRetryState
                ? ((latestSyncErrorMessage != null && !latestSyncErrorMessage.trim().isEmpty())
                ? latestSyncErrorMessage
                : getString(R.string.explore_load_failed))
                : getString(R.string.explore_load_failed));
        boolean showSearchEmptyState = lastCategoriesEmpty && !showRetryState;
        binding.textExploreEmpty.setText(hasSearchQuery
                ? R.string.explore_no_results
                : R.string.explore_load_failed);
        binding.textExploreEmpty.setVisibility(showSearchEmptyState ? View.VISIBLE : View.GONE);
        binding.buttonExploreRetry.setEnabled(!syncing);
    }
}
