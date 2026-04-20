package hcmute.edu.vn.nguyenthetan.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.usecase.explore.GetExploreCatalogUseCase;

public class ExploreViewModel extends ViewModel {

    private final GetExploreCatalogUseCase getExploreCatalogUseCase;
    private final MutableLiveData<List<ExploreCategory>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<ExploreCategory> allCategories = new ArrayList<>();
    private boolean loaded;
    private String searchQuery = "";
    private boolean showListening = true;
    private boolean showSpeaking = true;

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
            List<ExploreCategory> loadedCategories = new ArrayList<>(getExploreCatalogUseCase.execute());
            Collections.sort(loadedCategories, Comparator
                    .comparingInt(this::getPracticeOrder)
                    .thenComparing(category -> category.getTitle() == null ? "" : category.getTitle(), String.CASE_INSENSITIVE_ORDER));
            synchronized (allCategories) {
                allCategories.clear();
                allCategories.addAll(loadedCategories);
            }
            applyFilters();
            loadingState.postValue(false);
        });
        loaded = true;
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim();
        applyFilters();
    }

    public void setPracticeFilters(boolean listeningEnabled, boolean speakingEnabled) {
        showListening = listeningEnabled;
        showSpeaking = speakingEnabled;
        applyFilters();
    }

    public void forceLoad() {
        loaded = false;
        load();
    }

    private void applyFilters() {
        List<ExploreCategory> filtered = new ArrayList<>();
        String normalizedQuery = searchQuery.toLowerCase(Locale.US);
        synchronized (allCategories) {
            for (ExploreCategory category : allCategories) {
                if (!matchesPracticeFilter(category)) {
                    continue;
                }
                if (!matchesQuery(category, normalizedQuery)) {
                    continue;
                }
                filtered.add(category);
            }
        }
        categories.postValue(filtered);
    }

    private boolean matchesPracticeFilter(ExploreCategory category) {
        boolean listeningCategory = isListeningCategory(category);
        boolean speakingCategory = isSpeakingCategory(category);

        // If no filter is selected (shouldn't happen with UI guards), show nothing or everything?
        // Fragment prevents unchecking both, but let's be safe.
        if (!showListening && !showSpeaking) return false;

        // If both filters are selected, show categories that have either type
        if (showListening && showSpeaking) return true;

        // If only listening is selected, show only if it has listening
        if (showListening) return listeningCategory;

        // If only speaking is selected, show only if it has speaking
        if (showSpeaking) return speakingCategory;

        return true;
    }

    private boolean matchesQuery(ExploreCategory category, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        return safeValue(category.getTitle()).contains(normalizedQuery)
                || safeValue(category.getLevelRange()).contains(normalizedQuery)
                || safeValue(category.getPracticeType()).contains(normalizedQuery);
    }

    private int getPracticeOrder(ExploreCategory category) {
        if (isListeningCategory(category)) {
            return 0;
        }
        if (isSpeakingCategory(category)) {
            return 1;
        }
        return 2;
    }

    private boolean isListeningCategory(ExploreCategory category) {
        return safeValue(category.getPracticeType()).contains("listen");
    }

    private boolean isSpeakingCategory(ExploreCategory category) {
        return safeValue(category.getPracticeType()).contains("speak");
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
