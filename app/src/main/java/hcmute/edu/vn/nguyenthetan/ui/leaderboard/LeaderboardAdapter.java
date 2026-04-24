package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.databinding.ItemLeaderboardEntryBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardEntry;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void submitList(List<LeaderboardEntry> entries) {
        items.clear();
        items.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemLeaderboardEntryBinding.inflate(inflater, parent, false));
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

        private final ItemLeaderboardEntryBinding binding;

        ViewHolder(ItemLeaderboardEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardEntry item) {
            binding.textRank.setText(String.valueOf(item.getRank()));
            binding.textAvatar.setText(item.getAvatarLabel());
            binding.textName.setText(item.getName());
            binding.textTime.setText(item.getActiveTime());
            int backgroundColor = item.isCurrentUser()
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimaryTint)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorSurface);
            int strokeColor = item.isCurrentUser()
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimary)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorBorder);
            binding.getRoot().setCardBackgroundColor(backgroundColor);
            binding.getRoot().setStrokeColor(strokeColor);
        }
    }
}
