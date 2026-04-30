package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.databinding.ItemTranscriptSentenceBinding;

import android.view.View;

import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class TranscriptAdapter extends ListAdapter<LessonTranscriptRow, TranscriptAdapter.ViewHolder> {

    public interface Listener {
        void onSentenceSelected(int position);

        void onPlayRow(int position, boolean isCurrentlyPlaying);
    }

    private final Listener listener;
    private boolean showStatus = true;

    private static final DiffUtil.ItemCallback<LessonTranscriptRow> DIFF_CALLBACK = new DiffUtil.ItemCallback<LessonTranscriptRow>() {
        @Override
        public boolean areItemsTheSame(@NonNull LessonTranscriptRow oldItem, @NonNull LessonTranscriptRow newItem) {
            return oldItem.order == newItem.order;
        }

        @Override
        public boolean areContentsTheSame(@NonNull LessonTranscriptRow oldItem, @NonNull LessonTranscriptRow newItem) {
            return oldItem.selected == newItem.selected &&
                   oldItem.playing == newItem.playing &&
                   java.util.Objects.equals(oldItem.status, newItem.status) &&
                   java.util.Objects.equals(oldItem.sentence, newItem.sentence);
        }
    };

    public TranscriptAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemTranscriptSentenceBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener, showStatus);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemTranscriptSentenceBinding binding;

        ViewHolder(ItemTranscriptSentenceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LessonTranscriptRow row, Listener listener, boolean showStatus) {
            binding.textOrder.setText(String.valueOf(row.order));
            binding.textSentence.setText(row.sentence);
            binding.textStatus.setVisibility(showStatus ? View.VISIBLE : View.GONE);
            if (showStatus) {
                binding.textStatus.setText(row.status.name().replace('_', ' '));
            }
            binding.getRoot().setCardBackgroundColor(row.selected
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimaryTint)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorSurface));
            binding.getRoot().setStrokeColor(row.selected
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimary)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorBorder));
            binding.getRoot().setOnClickListener(v -> listener.onSentenceSelected(getBindingAdapterPosition()));
            binding.buttonPlayRow.setImageResource(
                    row.selected && row.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
            );
            binding.buttonPlayRow.setOnClickListener(v -> listener.onPlayRow(getBindingAdapterPosition(), row.selected && row.playing));
        }
    }
}
