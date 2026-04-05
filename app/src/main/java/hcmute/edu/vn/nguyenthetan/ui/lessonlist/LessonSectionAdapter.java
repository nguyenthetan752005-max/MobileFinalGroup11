package hcmute.edu.vn.nguyenthetan.ui.lessonlist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public LessonSectionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<LessonSection> sections) {
        items.clear();
        items.addAll(sections);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemLessonSectionBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemLessonSectionBinding binding;

        ViewHolder(ItemLessonSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LessonSection section, Listener listener) {
            binding.textSectionTitle.setText(section.getTitle());
            boolean expanded = listener.isExpanded(section.getId());
            binding.textArrow.setText(expanded ? "▴" : "▾");
            binding.cardHeader.setOnClickListener(v -> listener.onSectionToggled(section.getId()));
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
                        String.format(Locale.US, "%d/%d sentences", lesson.getCompletedSentences(), lesson.getTotalSentences())
                );
                lessonBinding.getRoot().setOnClickListener(v -> listener.onLessonSelected(lesson.getLessonId()));
                binding.lessonContainer.addView(lessonBinding.getRoot());
            }
        }
    }
}
