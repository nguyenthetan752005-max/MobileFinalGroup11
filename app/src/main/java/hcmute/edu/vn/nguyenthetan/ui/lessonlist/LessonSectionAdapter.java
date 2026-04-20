package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.databinding.ItemLessonSectionBinding;
import hcmute.edu.vn.nguyenthetan.databinding.ItemLessonSummaryBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonSection;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonSummary;

public class LessonSectionAdapter extends RecyclerView.Adapter<LessonSectionAdapter.ViewHolder> {

    public interface Listener {
        void onSectionToggled(long sectionId);

        boolean isExpanded(long sectionId);

        void onLessonSelected(long lessonId);
    }

    private final Listener listener;
    private final List<LessonSection> items = new ArrayList<>();
    private final List<LessonSection> sourceItems = new ArrayList<>();
    private String searchQuery = "";

    public LessonSectionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<LessonSection> sections) {
        sourceItems.clear();
        sourceItems.addAll(sections);
        applyFilter();
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);
        applyFilter();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemLessonSectionBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener, searchQuery);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyFilter() {
        items.clear();
        if (searchQuery.isEmpty()) {
            items.addAll(sourceItems);
            notifyDataSetChanged();
            return;
        }

        for (LessonSection section : sourceItems) {
            List<LessonSummary> visibleLessons = new ArrayList<>();
            for (LessonSummary lesson : section.getLessons()) {
                String lessonTitle = lesson.getTitle() == null ? "" : lesson.getTitle().toLowerCase(Locale.US);
                String lessonLevel = lesson.getLevel() == null ? "" : lesson.getLevel().toLowerCase(Locale.US);
                String practiceType = lesson.getPracticeType() == null ? "" : lesson.getPracticeType().toLowerCase(Locale.US);
                if (lessonTitle.contains(searchQuery)
                        || lessonLevel.contains(searchQuery)
                        || practiceType.contains(searchQuery)) {
                    visibleLessons.add(lesson);
                }
            }
            if (!visibleLessons.isEmpty()) {
                items.add(new LessonSection(section.getId(), section.getTitle(), visibleLessons));
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemLessonSectionBinding binding;

        ViewHolder(ItemLessonSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LessonSection section, Listener listener, String searchQuery) {
            binding.textSectionTitle.setText(section.getTitle());
            boolean expanded = searchQuery.isEmpty() ? listener.isExpanded(section.getId()) : true;
            binding.textArrow.setText(expanded ? "\u25B4" : "\u25BE");
            binding.cardHeader.setOnClickListener(searchQuery.isEmpty()
                    ? v -> listener.onSectionToggled(section.getId())
                    : null);
            binding.lessonContainer.removeAllViews();

            if (!expanded) {
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
            for (LessonSummary lesson : section.getLessons()) {
                ItemLessonSummaryBinding lessonBinding = ItemLessonSummaryBinding.inflate(inflater, binding.lessonContainer, false);
                lessonBinding.textTitle.setText(lesson.getTitle());
                lessonBinding.textLevel.setText(lesson.getLevel());
                lessonBinding.textPracticeType.setText(lesson.getPracticeType());
                lessonBinding.textStatus.setText(lesson.getStatus().name().replace('_', ' '));
                lessonBinding.progressLesson.setMax(lesson.getTotalSentences());
                lessonBinding.progressLesson.setProgressCompat(lesson.getCompletedSentences(), true);
                lessonBinding.textProgress.setText(
                        binding.getRoot().getContext().getString(
                                R.string.lesson_progress_format,
                                lesson.getCompletedSentences(),
                                lesson.getTotalSentences()
                        )
                );
                lessonBinding.getRoot().setOnClickListener(v -> listener.onLessonSelected(lesson.getLessonId()));
                binding.lessonContainer.addView(lessonBinding.getRoot());
            }
        }
    }
}
