package hcmute.edu.vn.nguyenthetan.ui.explore;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
            binding.imageThumbnail.setVisibility(View.GONE);
            if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
                binding.imageThumbnail.setVisibility(View.VISIBLE);
                Glide.with(binding.imageThumbnail)
                        .load(item.getImageUrl())
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                binding.imageThumbnail.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                binding.imageThumbnail.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(binding.imageThumbnail);
            } else {
                Glide.with(binding.imageThumbnail).clear(binding.imageThumbnail);
            }
            binding.getRoot().setOnClickListener(v -> listener.onCategorySelected(item));
        }
    }
}
