package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileSentenceDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName(value = "lessonId", alternate = {"lesson_id"})
    public String lessonId;

    @SerializedName(value = "audioUrl", alternate = {"audio_url"})
    public String audioUrl;

    @SerializedName("content")
    public String content;

    @SerializedName(value = "hintText", alternate = {"hint_text"})
    public String hintText;

    @SerializedName(value = "durationMillis", alternate = {"duration_millis"})
    public long durationMillis;

    @SerializedName(value = "startTime", alternate = {"start_time"})
    public Double startTime;

    @SerializedName(value = "endTime", alternate = {"end_time"})
    public Double endTime;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;

    @SerializedName("properNouns")
    public java.util.List<String> properNouns;
}
