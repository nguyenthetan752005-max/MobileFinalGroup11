package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLessonListBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.ui.lesson.LessonActivity;

public class LessonListActivity extends AppCompatActivity implements LessonSectionAdapter.Listener {

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private ActivityLessonListBinding binding;
    private LessonListViewModel viewModel;
    private LessonSectionAdapter adapter;

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

        adapter = new LessonSectionAdapter(this);
        binding.recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSections.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(v -> finish());

        TungTungApplication application = (TungTungApplication) getApplication();
        String categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        LessonListViewModelFactory factory = new LessonListViewModelFactory(
                application.getAppContainer().getLessonCollectionUseCase(),
                categoryId
        );
        viewModel = new ViewModelProvider(this, factory).get(LessonListViewModel.class);
        viewModel.getCollectionState().observe(this, this::render);
        viewModel.load();
    }

    private void render(@NonNull LessonCollection collection) {
        binding.textBreadcrumbTitle.setText(collection.getTitle());
        binding.textCategoryTitle.setText(collection.getTitle());
        binding.textCategoryDescription.setText(collection.getDescription());
        binding.textTotalLessons.setText(collection.getTotalLessons() + " lessons");
        adapter.submitList(collection.getSections());
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
}
