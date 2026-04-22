package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncCategoryCollectionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncSectionLessonsUseCase;

public class LessonListViewModelFactory implements ViewModelProvider.Factory {

    private final GetLessonCollectionUseCase getLessonCollectionUseCase;
    private final SyncCategoryCollectionUseCase syncCategoryCollectionUseCase;
    private final SyncSectionLessonsUseCase syncSectionLessonsUseCase;
    private final String categoryId;

    public LessonListViewModelFactory(
            GetLessonCollectionUseCase getLessonCollectionUseCase,
            SyncCategoryCollectionUseCase syncCategoryCollectionUseCase,
            SyncSectionLessonsUseCase syncSectionLessonsUseCase,
            String categoryId) {
        this.getLessonCollectionUseCase = getLessonCollectionUseCase;
        this.syncCategoryCollectionUseCase = syncCategoryCollectionUseCase;
        this.syncSectionLessonsUseCase = syncSectionLessonsUseCase;
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LessonListViewModel.class)) {
            return (T) new LessonListViewModel(
                    getLessonCollectionUseCase,
                    syncCategoryCollectionUseCase,
                    syncSectionLessonsUseCase,
                    categoryId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
