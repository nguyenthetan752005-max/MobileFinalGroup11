package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.databinding.ItemCommentBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public interface CommentActionListener {
        void onLike(long commentId);
        void onDislike(long commentId);
        void onReply(long commentId, String authorName);
        void onDelete(long commentId);
    }

    private final List<Comment> items = new ArrayList<>();
    private long currentUserId;
    @Nullable
    private CommentActionListener listener;

    public void setActionListener(@Nullable CommentActionListener listener) {
        this.listener = listener;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

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
        holder.bind(items.get(position), currentUserId, listener);
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

        void bind(Comment item, long currentUserId, @Nullable CommentActionListener listener) {
            binding.textAvatar.setText(item.getAvatarLabel());
            binding.textAuthor.setText(item.getAuthor());
            binding.textTime.setText(item.getTimeAgo());
            binding.textContent.setText(item.getContent());

            // Like/Dislike counts
            binding.textLikeCount.setText(String.valueOf(item.getLikes()));
            binding.textDislikeCount.setText(String.valueOf(item.getDislikes()));
            binding.textActions.setText("");

            // Wire buttons
            binding.buttonLike.setOnClickListener(v -> {
                if (listener != null) listener.onLike(item.getId());
            });
            binding.buttonDislike.setOnClickListener(v -> {
                if (listener != null) listener.onDislike(item.getId());
            });
            binding.buttonReply.setOnClickListener(v -> {
                if (listener != null) listener.onReply(item.getId(), item.getAuthor());
            });

            // Long-press to delete own comments
            if (currentUserId > 0 && item.getAuthorId() == currentUserId) {
                binding.getRoot().setOnLongClickListener(v -> {
                    if (listener != null) listener.onDelete(item.getId());
                    return true;
                });
            } else {
                binding.getRoot().setOnLongClickListener(null);
            }

            // Replies
            binding.repliesContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
            for (Comment reply : item.getReplies()) {
                ItemCommentBinding replyBinding = ItemCommentBinding.inflate(inflater, binding.repliesContainer, false);
                replyBinding.textAvatar.setText(reply.getAvatarLabel());
                replyBinding.textAuthor.setText(reply.getAuthor());
                replyBinding.textTime.setText(reply.getTimeAgo());
                replyBinding.textContent.setText(reply.getContent());
                replyBinding.textLikeCount.setText(String.valueOf(reply.getLikes()));
                replyBinding.textDislikeCount.setText(String.valueOf(reply.getDislikes()));
                replyBinding.textActions.setText("");
                replyBinding.repliesContainer.setVisibility(View.GONE);

                // Wire reply buttons
                replyBinding.buttonLike.setOnClickListener(v -> {
                    if (listener != null) listener.onLike(reply.getId());
                });
                replyBinding.buttonDislike.setOnClickListener(v -> {
                    if (listener != null) listener.onDislike(reply.getId());
                });
                replyBinding.buttonReply.setOnClickListener(v -> {
                    if (listener != null) listener.onReply(reply.getId(), reply.getAuthor());
                });

                // Long-press to delete own replies
                if (currentUserId > 0 && reply.getAuthorId() == currentUserId) {
                    replyBinding.getRoot().setOnLongClickListener(v -> {
                        if (listener != null) listener.onDelete(reply.getId());
                        return true;
                    });
                }

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
