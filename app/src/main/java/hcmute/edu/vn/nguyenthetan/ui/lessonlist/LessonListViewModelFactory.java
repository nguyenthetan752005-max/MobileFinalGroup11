package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;

public class LessonListViewModelFactory implements ViewModelProvider.Factory {

    private final GetLessonCollectionUseCase getLessonCollectionUseCase;
    private final String categoryId;

    public LessonListViewModelFactory(GetLessonCollectionUseCase getLessonCollectionUseCase, String categoryId) {
        this.getLessonCollectionUseCase = getLessonCollectionUseCase;
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LessonListViewModel.class)) {
            return (T) new LessonListViewModel(getLessonCollectionUseCase, categoryId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
