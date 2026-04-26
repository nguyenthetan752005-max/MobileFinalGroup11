package hcmute.edu.vn.nguyenthetan.ui.explore;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

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
        holder.bind(items.get(position), listener, position);
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

        void bind(ExploreCategory item, Listener listener, int position) {
            Context context = binding.imageThumbnail.getContext();
            binding.textProgress.setText(item.getProgressLabel());
            binding.textTitle.setText(item.getTitle());
            binding.textLevelRange.setText(item.getLevelRange());
            binding.textLessonCount.setText(item.getLessonCount() + " lessons");
            binding.chipPracticeType.setText(item.getPracticeType());
            loadThumbnail(item, context);
            binding.getRoot().setOnClickListener(v -> listener.onCategorySelected(item));
        }

        private void loadThumbnail(ExploreCategory item, Context context) {
            String imageUrl = normalizeUrl(item.getImageUrl());
            if (imageUrl == null) {
                Glide.with(binding.imageThumbnail).clear(binding.imageThumbnail);
                binding.imageThumbnail.setVisibility(View.GONE);
                return;
            }

            List<String> candidateUrls = new ArrayList<>();
            if (hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore.isScaryMoodActive(context)) {
                candidateUrls.addAll(buildScaryCandidates(imageUrl));
            }
            candidateUrls.add(imageUrl);

            binding.imageThumbnail.setVisibility(View.VISIBLE);
            buildRequestChain(candidateUrls, 0).into(binding.imageThumbnail);
        }

        private RequestBuilder<Drawable> buildRequestChain(List<String> candidateUrls, int index) {
            RequestBuilder<Drawable> request = Glide.with(binding.imageThumbnail)
                    .load(candidateUrls.get(index))
                    .centerCrop();

            if (index == candidateUrls.size() - 1) {
                return request.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        binding.imageThumbnail.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        binding.imageThumbnail.setVisibility(View.VISIBLE);
                        return false;
                    }
                });
            }

            return request.error(buildRequestChain(candidateUrls, index + 1));
        }

        private List<String> buildScaryCandidates(String imageUrl) {
            List<String> candidates = new ArrayList<>();

            int queryIndex = imageUrl.indexOf('?');
            String path = queryIndex >= 0 ? imageUrl.substring(0, queryIndex) : imageUrl;
            String query = queryIndex >= 0 ? imageUrl.substring(queryIndex) : "";

            int extensionIndex = path.lastIndexOf('.');
            if (extensionIndex <= 0) {
                return candidates;
            }

            String basePath = path.substring(0, extensionIndex);
            addCandidate(candidates, basePath + "_scary.jpg" + query, imageUrl);
            return candidates;
        }

        private void addCandidate(List<String> candidates, String candidate, String originalUrl) {
            if (candidate == null || candidate.equals(originalUrl) || candidates.contains(candidate)) {
                return;
            }
            candidates.add(candidate);
        }

        @Nullable
        private String normalizeUrl(@Nullable String imageUrl) {
            if (imageUrl == null) {
                return null;
            }
            String trimmed = imageUrl.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
