package hcmute.edu.vn.nguyenthetan.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.databinding.ItemRecommendationBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.home.Recommendation;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final List<Recommendation> items = new ArrayList<>();

    public void submitList(List<Recommendation> recommendations) {
        items.clear();
        items.addAll(recommendations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemRecommendationBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecommendationBinding binding;

        ViewHolder(ItemRecommendationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recommendation item) {
            binding.textTitle.setText(item.getTitle());
            binding.textLevel.setText(item.getLevel());
            binding.textLessonCount.setText(item.getLessonCount() + " lessons");
            binding.chipPracticeType.setText(item.getPracticeType());
        }
    }
}
