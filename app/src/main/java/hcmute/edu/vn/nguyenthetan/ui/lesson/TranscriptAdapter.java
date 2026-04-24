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
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {

    public interface Listener {
        void onSentenceSelected(int position);

        void onPlayRow(int position);
    }

    public static class Row {
        public final int order;
        public final String sentence;
        public final SentenceStatus status;
        public final boolean selected;

        public Row(int order, String sentence, SentenceStatus status, boolean selected) {
            this.order = order;
            this.sentence = sentence;
            this.status = status;
            this.selected = selected;
        }
    }

    private final Listener listener;
    private final List<Row> items = new ArrayList<>();

    public TranscriptAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Row> rows) {
        items.clear();
        items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemTranscriptSentenceBinding.inflate(inflater, parent, false));
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

        private final ItemTranscriptSentenceBinding binding;

        ViewHolder(ItemTranscriptSentenceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Row row, Listener listener) {
            binding.textOrder.setText(String.valueOf(row.order));
            binding.textSentence.setText(row.sentence);
            binding.textStatus.setText(row.status.name().replace('_', ' '));
            binding.getRoot().setCardBackgroundColor(row.selected
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimaryTint)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorSurface));
            binding.getRoot().setStrokeColor(row.selected
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimary)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorBorder));
            binding.getRoot().setOnClickListener(v -> listener.onSentenceSelected(getBindingAdapterPosition()));
            binding.buttonPlayRow.setOnClickListener(v -> listener.onPlayRow(getBindingAdapterPosition()));
        }
    }
}
