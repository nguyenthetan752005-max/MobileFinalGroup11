package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class DictationResultDto {
    @SerializedName("correct")
    public boolean correct;

    @SerializedName(value = "matchedCount", alternate = {"matched_count"})
    public int matchedCount;

    @SerializedName("hint")
    public String hint;

    @SerializedName(value = "correctSentence", alternate = {"correct_sentence"})
    public String correctSentence;

    @SerializedName("message")
    public String message;
}
