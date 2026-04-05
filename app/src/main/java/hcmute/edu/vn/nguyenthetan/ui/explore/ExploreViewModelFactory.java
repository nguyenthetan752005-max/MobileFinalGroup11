package hcmute.edu.vn.nguyenthetan.ui.explore;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;

public class ExploreViewModelFactory implements ViewModelProvider.Factory {

    private final GetExploreCatalogUseCase getExploreCatalogUseCase;

    public ExploreViewModelFactory(GetExploreCatalogUseCase getExploreCatalogUseCase) {
        this.getExploreCatalogUseCase = getExploreCatalogUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExploreViewModel.class)) {
            return (T) new ExploreViewModel(getExploreCatalogUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
