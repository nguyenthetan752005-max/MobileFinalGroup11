package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLessonListBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;

public class LessonListActivity extends ThemedActivity implements LessonSectionAdapter.Listener {

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private ActivityLessonListBinding binding;
    private LessonListViewModel viewModel;
    private LessonSectionAdapter adapter;
    private Boolean lastSyncing;
    private boolean lastLoading;
    private boolean lastCollectionEmpty = true;
    private String latestErrorMessage;

    public static Intent newIntent(Context context, String categoryId) {
        Intent intent = new Intent(context, LessonListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLessonListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        adapter = new LessonSectionAdapter(this);
        binding.recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSections.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.inputLessonSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s == null ? "" : s.toString());
                updateLessonSearchEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TungTungApplication application = (TungTungApplication) getApplication();
        String categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        LessonListViewModelFactory factory = new LessonListViewModelFactory(
                application.getAppContainer().getLessonCollectionUseCase(),
                application.getAppContainer().getSyncCategoryCollectionUseCase(),
                application.getAppContainer().getSyncSectionLessonsUseCase(),
                categoryId
        );
        viewModel = new ViewModelProvider(this, factory).get(LessonListViewModel.class);
        viewModel.getCollectionState().observe(this, this::render);
        viewModel.getLoadingState().observe(this, loading -> {
            lastLoading = Boolean.TRUE.equals(loading);
            binding.progressLoad.setVisibility(lastLoading ? View.VISIBLE : View.GONE);
            renderLessonListState();
        });
        viewModel.getErrorState().observe(this, errorMessage -> {
            latestErrorMessage = errorMessage;
            renderLessonListState();
        });
        application.getAppContainer().getSyncErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
        application.getAppContainer().getIsSyncing().observe(this, syncing -> {
            if (Boolean.TRUE.equals(lastSyncing) && !Boolean.TRUE.equals(syncing) && viewModel != null && lastCollectionEmpty) {
                viewModel.retry();
            }
            lastSyncing = syncing;
            renderLessonListState();
        });
        binding.buttonLessonListRetry.setOnClickListener(v -> {
            latestErrorMessage = null;
            renderLessonListState();
            viewModel.retry();
        });
        viewModel.load();
    }

    private void render(@NonNull LessonCollection collection) {
        binding.textBreadcrumbTitle.setText(collection.getTitle());
        binding.textCategoryTitle.setText(collection.getTitle());
        binding.textCategoryDescription.setText(collection.getDescription());
        binding.textTotalLessons.setText(collection.getTotalLessons() + " lessons");
        adapter.submitList(collection.getSections());
        lastCollectionEmpty = collection.getSections() == null || collection.getSections().isEmpty();
        if (!lastCollectionEmpty) {
            latestErrorMessage = null;
        }
        updateLessonSearchEmptyState();
        renderLessonListState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshLocal();
        }
    }

    @Override
    public void onSectionToggled(long sectionId) {
        viewModel.toggleSection(sectionId);
    }

    @Override
    public boolean isExpanded(long sectionId) {
        return viewModel.isExpanded(sectionId);
    }

    @Override
    public void onLessonSelected(long lessonId) {
        startActivity(LessonActivity.newIntent(this, lessonId));
    }

    private void updateLessonSearchEmptyState() {
        String query = binding.inputLessonSearch.getText() == null
                ? ""
                : binding.inputLessonSearch.getText().toString().trim();
        boolean showEmptyState = !query.isEmpty()
                && adapter.getItemCount() == 0
                && binding.layoutLessonListError.getVisibility() != View.VISIBLE;
        binding.textNoLessonResults.setVisibility(showEmptyState ? View.VISIBLE : View.GONE);
    }

    private void renderLessonListState() {
        boolean syncing = Boolean.TRUE.equals(lastSyncing);
        boolean showError = lastCollectionEmpty
                && !lastLoading
                && !syncing
                && latestErrorMessage != null
                && !latestErrorMessage.trim().isEmpty();
        binding.layoutLessonListError.setVisibility(showError ? View.VISIBLE : View.GONE);
        binding.textLessonListError.setText(showError
                ? latestErrorMessage
                : getString(hcmute.edu.vn.nguyenthetan.R.string.lesson_list_load_failed));
        binding.buttonLessonListRetry.setEnabled(!lastLoading && !syncing);
        if (showError) {
            binding.textNoLessonResults.setVisibility(View.GONE);
        }
    }
}

