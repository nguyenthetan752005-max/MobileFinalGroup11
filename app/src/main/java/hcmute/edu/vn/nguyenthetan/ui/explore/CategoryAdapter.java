package hcmute.edu.vn.nguyenthetan.ui.explore;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.databinding.ItemExploreCategoryBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface Listener {
        void onCategorySelected(ExploreCategory category);
    }

    private final Listener listener;
    private final List<ExploreCategory> items = new ArrayList<>();

    public CategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<ExploreCategory> categories) {
        items.clear();
        items.addAll(categories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemExploreCategoryBinding.inflate(inflater, parent, false));
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

        private final ItemExploreCategoryBinding binding;

        ViewHolder(ItemExploreCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ExploreCategory item, Listener listener) {
            binding.textProgress.setText(item.getProgressLabel());
            binding.textTitle.setText(item.getTitle());
            binding.textLevelRange.setText(item.getLevelRange());
            binding.textLessonCount.setText(item.getLessonCount() + " lessons");
            binding.chipPracticeType.setText(item.getPracticeType());
            binding.getRoot().setOnClickListener(v -> listener.onCategorySelected(item));
        }
    }
}
