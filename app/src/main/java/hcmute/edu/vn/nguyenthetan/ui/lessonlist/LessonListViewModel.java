package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.Set;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonCollectionUseCase;

public class LessonListViewModel extends ViewModel {

    private final GetLessonCollectionUseCase getLessonCollectionUseCase;
    private final String categoryId;
    private final MutableLiveData<LessonCollection> collectionState = new MutableLiveData<>();
    private final Set<Long> expandedSectionIds = new HashSet<>();
    private boolean loaded;

    public LessonListViewModel(GetLessonCollectionUseCase getLessonCollectionUseCase, String categoryId) {
        this.getLessonCollectionUseCase = getLessonCollectionUseCase;
        this.categoryId = categoryId;
    }

    public LiveData<LessonCollection> getCollectionState() {
        return collectionState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        LessonCollection collection = getLessonCollectionUseCase.execute(categoryId);
        for (int index = 0; index < collection.getSections().size(); index++) {
            if (index == 0) {
                expandedSectionIds.add(collection.getSections().get(index).getId());
            }
        }
        collectionState.setValue(collection);
        loaded = true;
    }

    public boolean isExpanded(long sectionId) {
        return expandedSectionIds.contains(sectionId);
    }

    public void toggleSection(long sectionId) {
        if (expandedSectionIds.contains(sectionId)) {
            expandedSectionIds.remove(sectionId);
        } else {
            expandedSectionIds.add(sectionId);
        }
        collectionState.setValue(collectionState.getValue());
    }
}
