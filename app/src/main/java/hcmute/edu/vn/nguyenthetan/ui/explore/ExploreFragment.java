package hcmute.edu.vn.nguyenthetan.ui.explore;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.FragmentExploreBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;

public class ExploreFragment extends Fragment implements CategoryAdapter.Listener {

    public interface Listener {
        void onOpenCategory(String categoryId);
    }

    private FragmentExploreBinding binding;
    private Listener listener;

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
        binding.recyclerCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerCategories.setAdapter(adapter);

        TungTungApplication application = (TungTungApplication) requireActivity().getApplication();
        ExploreViewModelFactory factory = new ExploreViewModelFactory(application.getAppContainer().getExploreCatalogUseCase());
        ExploreViewModel viewModel = new ViewModelProvider(this, factory).get(ExploreViewModel.class);
        viewModel.getCategories().observe(getViewLifecycleOwner(), adapter::submitList);
        
        application.getAppContainer().getIsSyncing().observe(getViewLifecycleOwner(), syncing -> {
            if (!syncing) {
                viewModel.forceLoad();
            }
        });
        
        viewModel.load();
    }

    @Override
    public void onCategorySelected(ExploreCategory category) {
        if (listener != null) {
            listener.onOpenCategory(category.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
