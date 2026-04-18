package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileBootstrapCommentDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName(value = "sentenceId", alternate = {"sentence_id"})
    public String sentenceId;

    @SerializedName(value = "parentCommentId", alternate = {"parentComment_id", "parent_id"})
    public String parentCommentId;

    @SerializedName(value = "author", alternate = {"authorName", "author_name"})
    public String author;

    @SerializedName(value = "avatarLabel", alternate = {"avatar_label"})
    public String avatarLabel;

    @SerializedName(value = "timeAgo", alternate = {"time_ago"})
    public String timeAgo;

    @SerializedName("content")
    public String content;

    @SerializedName(value = "likeCount", alternate = {"like_count"})
    public int likeCount;

    @SerializedName(value = "dislikeCount", alternate = {"dislike_count"})
    public int dislikeCount;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;
}
