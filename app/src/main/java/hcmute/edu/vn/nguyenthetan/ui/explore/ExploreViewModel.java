package hcmute.edu.vn.nguyenthetan.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;

public class ExploreViewModel extends ViewModel {

    private final GetExploreCatalogUseCase getExploreCatalogUseCase;
    private final MutableLiveData<List<ExploreCategory>> categories = new MutableLiveData<>();
    private boolean loaded;

    public ExploreViewModel(GetExploreCatalogUseCase getExploreCatalogUseCase) {
        this.getExploreCatalogUseCase = getExploreCatalogUseCase;
    }

    public LiveData<List<ExploreCategory>> getCategories() {
        return categories;
    }

    public void load() {
        if (loaded) {
            return;
        }
        categories.setValue(getExploreCatalogUseCase.execute());
        loaded = true;
    }

    public void forceLoad() {
        loaded = false;
        load();
    }
}
