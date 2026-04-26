package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncCategoryCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncSectionLessonsUseCase;

public class LessonListViewModel extends ViewModel {

    private final GetLessonCollectionUseCase getLessonCollectionUseCase;
    private final SyncCategoryCollectionUseCase syncCategoryCollectionUseCase;
    private final SyncSectionLessonsUseCase syncSectionLessonsUseCase;
    private final String categoryId;
    private final MutableLiveData<LessonCollection> collectionState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final Set<Long> expandedSectionIds = new HashSet<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public LessonListViewModel(
            GetLessonCollectionUseCase getLessonCollectionUseCase,
            SyncCategoryCollectionUseCase syncCategoryCollectionUseCase,
            SyncSectionLessonsUseCase syncSectionLessonsUseCase,
            String categoryId) {
        this.getLessonCollectionUseCase = getLessonCollectionUseCase;
        this.syncCategoryCollectionUseCase = syncCategoryCollectionUseCase;
        this.syncSectionLessonsUseCase = syncSectionLessonsUseCase;
        this.categoryId = categoryId;
    }

    public LiveData<LessonCollection> getCollectionState() {
        return collectionState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadingState.setValue(true);
        executorService.execute(() -> {
            // Show cached data immediately
            LessonCollection cached = getLessonCollectionUseCase.execute(categoryId);
            if (cached != null && !cached.getSections().isEmpty()) {
                // Auto-expand first section
                expandedSectionIds.add(cached.getSections().get(0).getId());
                collectionState.postValue(cached);
            }
            // Sync in background and refresh
            syncCategoryCollectionUseCase.execute(categoryId);
            LessonCollection fresh = getLessonCollectionUseCase.execute(categoryId);
            if (fresh != null && !fresh.getSections().isEmpty() && expandedSectionIds.isEmpty()) {
                expandedSectionIds.add(fresh.getSections().get(0).getId());
            }
            collectionState.postValue(fresh);
            loadingState.postValue(false);
        });
    }

    public boolean isExpanded(long sectionId) {
        return expandedSectionIds.contains(sectionId);
    }

    public void toggleSection(long sectionId) {
        if (expandedSectionIds.contains(sectionId)) {
            expandedSectionIds.remove(sectionId);
            collectionState.setValue(collectionState.getValue());
        } else {
            expandedSectionIds.add(sectionId);
            loadingState.setValue(true);
            executorService.execute(() -> {
                syncSectionLessonsUseCase.execute(sectionId);
                LessonCollection collection = getLessonCollectionUseCase.execute(categoryId);
                collectionState.postValue(collection);
                loadingState.postValue(false);
            });
        }
    }

    public void refreshLocal() {
        executorService.execute(() -> {
            LessonCollection cached = getLessonCollectionUseCase.execute(categoryId);
            if (cached != null) {
                collectionState.postValue(cached);
            }
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
