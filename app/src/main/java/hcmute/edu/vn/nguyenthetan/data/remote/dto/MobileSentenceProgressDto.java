package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileSentenceProgressDto {

    @SerializedName(value = "sentenceId", alternate = {"sentence_id"})
    public long sentenceId;

    @SerializedName(value = "lessonId", alternate = {"lesson_id"})
    public long lessonId;

    @SerializedName("status")
    public String status;
}
