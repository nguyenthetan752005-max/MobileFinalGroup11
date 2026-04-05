package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.databinding.ItemCommentBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<Comment> items = new ArrayList<>();

    public void submitList(List<Comment> comments) {
        items.clear();
        items.addAll(comments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemCommentBinding.inflate(inflater, parent, false));
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

        private final ItemCommentBinding binding;

        ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Comment item) {
            binding.textAvatar.setText(item.getAvatarLabel());
            binding.textAuthor.setText(item.getAuthor());
            binding.textTime.setText(item.getTimeAgo());
            binding.textContent.setText(item.getContent());
            binding.textActions.setText("Like " + item.getLikes() + " • Dislike " + item.getDislikes() + " • Reply");
            binding.repliesContainer.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
            for (Comment reply : item.getReplies()) {
                ItemCommentBinding replyBinding = ItemCommentBinding.inflate(inflater, binding.repliesContainer, false);
                replyBinding.textAvatar.setText(reply.getAvatarLabel());
                replyBinding.textAuthor.setText(reply.getAuthor());
                replyBinding.textTime.setText(reply.getTimeAgo());
                replyBinding.textContent.setText(reply.getContent());
                replyBinding.textActions.setText("Like " + reply.getLikes() + " | Reply");
                replyBinding.repliesContainer.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) replyBinding.getRoot().getLayoutParams();
                if (params == null) {
                    params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
                params.setMargins(dp(16), dp(8), 0, 0);
                replyBinding.getRoot().setLayoutParams(params);
                binding.repliesContainer.addView(replyBinding.getRoot());
            }
        }

        private int dp(int value) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    binding.getRoot().getResources().getDisplayMetrics()
            );
        }
    }
}
