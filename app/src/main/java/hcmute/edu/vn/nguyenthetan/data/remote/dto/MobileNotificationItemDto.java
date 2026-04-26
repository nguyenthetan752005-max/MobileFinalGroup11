package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileNotificationItemDto {
    @SerializedName("id")
    public long id;

    @SerializedName("type")
    public String type;

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    @SerializedName("meta")
    public String meta;

    @SerializedName(value = "timeAgo", alternate = {"time_ago"})
    public String timeAgo;

    @SerializedName("read")
    public Boolean read;

    @SerializedName(value = "targetLessonId", alternate = {"target_lesson_id"})
    public Long targetLessonId;

    @SerializedName(value = "targetSentenceId", alternate = {"target_sentence_id"})
    public Long targetSentenceId;

    @SerializedName(value = "targetCommentId", alternate = {"target_comment_id"})
    public Long targetCommentId;
}
