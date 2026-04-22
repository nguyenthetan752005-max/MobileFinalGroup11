package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;

public class MyCommentsAdapter extends RecyclerView.Adapter<MyCommentsAdapter.ViewHolder> {

    private final List<MobileBootstrapCommentDto> comments = new ArrayList<>();

    public void submitList(List<MobileBootstrapCommentDto> list) {
        comments.clear();
        if (list != null) {
            comments.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MobileBootstrapCommentDto comment = comments.get(position);
        holder.textTime.setText(comment.timeAgo);
        holder.textContent.setText(comment.content);
        holder.textLikeCount.setText(String.valueOf(comment.likeCount));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textTime;
        final TextView textContent;
        final TextView textLikeCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.textTime);
            textContent = itemView.findViewById(R.id.textContent);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
        }
    }
}
