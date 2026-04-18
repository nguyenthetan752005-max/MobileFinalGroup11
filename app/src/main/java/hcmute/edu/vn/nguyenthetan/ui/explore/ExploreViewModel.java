package hcmute.edu.vn.nguyenthetan.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;

public class ExploreViewModel extends ViewModel {

    private final GetExploreCatalogUseCase getExploreCatalogUseCase;
    private final MutableLiveData<List<ExploreCategory>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public ExploreViewModel(GetExploreCatalogUseCase getExploreCatalogUseCase) {
        this.getExploreCatalogUseCase = getExploreCatalogUseCase;
    }

    public LiveData<List<ExploreCategory>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        loadingState.setValue(true);
        executorService.execute(() -> {
            categories.postValue(getExploreCatalogUseCase.execute());
            loadingState.postValue(false);
        });
        loaded = true;
    }

    public void forceLoad() {
        loaded = false;
        load();
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
