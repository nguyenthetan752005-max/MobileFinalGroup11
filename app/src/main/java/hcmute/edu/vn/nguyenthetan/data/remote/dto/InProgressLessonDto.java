package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class InProgressLessonDto {
    @SerializedName(value = "lessonId", alternate = {"lesson_id"})
    public long lessonId;

    @SerializedName("title")
    public String title;

    @SerializedName("level")
    public String level;

    @SerializedName(value = "completedSentences", alternate = {"completed_sentences"})
    public int completedSentences;

    @SerializedName(value = "totalSentences", alternate = {"total_sentences"})
    public int totalSentences;
}
