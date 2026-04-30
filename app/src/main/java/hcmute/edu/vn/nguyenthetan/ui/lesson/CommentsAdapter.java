package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hcmute.edu.vn.nguyenthetan.databinding.ItemCommentBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private static final Pattern REPLY_PREFIX_PATTERN = Pattern.compile("^@\\[([^\\]]+)\\]\\s+(.*)$", Pattern.DOTALL);

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
            bindAvatar(binding, item);
            binding.textAuthor.setText(item.getAuthor());
            binding.textTime.setText(item.getTimeAgo());
            bindReplyContent(binding, item.getContent());

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
                bindAvatar(replyBinding, reply);
                replyBinding.textAuthor.setText(reply.getAuthor());
                replyBinding.textTime.setText(reply.getTimeAgo());
                bindReplyContent(replyBinding, reply.getContent());
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
                    if (listener != null) listener.onReply(item.getId(), reply.getAuthor());
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
                params.setMargins(0, dp(8), 0, 0);
                replyBinding.getRoot().setLayoutParams(params);
                binding.repliesContainer.addView(replyBinding.getRoot());
            }
        }

        private void bindAvatar(ItemCommentBinding itemBinding, Comment item) {
            String avatarUrl = item.getAvatarUrl();
            String avatarLabel = item.getAvatarLabel();
            itemBinding.textAvatar.setText(avatarLabel);
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                itemBinding.imageAvatar.setVisibility(View.VISIBLE);
                itemBinding.textAvatar.setVisibility(View.GONE);
                Glide.with(itemBinding.imageAvatar)
                        .load(avatarUrl.trim())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .circleCrop()
                        .into(itemBinding.imageAvatar);
            } else {
                Glide.with(itemBinding.imageAvatar).clear(itemBinding.imageAvatar);
                itemBinding.imageAvatar.setVisibility(View.GONE);
                itemBinding.textAvatar.setVisibility(View.VISIBLE);
            }
        }

        private void bindReplyContent(ItemCommentBinding itemBinding, String rawContent) {
            String content = rawContent == null ? "" : rawContent.trim();
            Matcher matcher = REPLY_PREFIX_PATTERN.matcher(content);
            if (matcher.matches()) {
                itemBinding.textReplyTarget.setVisibility(View.VISIBLE);
                itemBinding.textReplyTarget.setText("Replying to @" + matcher.group(1));
                itemBinding.textContent.setText(matcher.group(2).trim());
                return;
            }
            itemBinding.textReplyTarget.setVisibility(View.GONE);
            itemBinding.textReplyTarget.setText("");
            itemBinding.textContent.setText(content);
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
