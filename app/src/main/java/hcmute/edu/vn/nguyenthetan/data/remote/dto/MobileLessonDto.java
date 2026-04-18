package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileLessonDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName(value = "sectionId", alternate = {"section_id"})
    public String sectionId;

    @SerializedName("title")
    public String title;

    @SerializedName("level")
    public String level;

    @SerializedName(value = "displayType", alternate = {"display_type", "practiceType", "practice_type"})
    public String displayType;

    @SerializedName(value = "contentType", alternate = {"content_type", "type"})
    public String contentType;

    @SerializedName(value = "totalSentences", alternate = {"total_sentences"})
    public int totalSentences;

    @SerializedName(value = "passThreshold", alternate = {"pass_threshold"})
    public int passThreshold;

    @SerializedName(value = "youtubeVideoId", alternate = {"youtube_video_id"})
    public String youtubeVideoId;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;
}
