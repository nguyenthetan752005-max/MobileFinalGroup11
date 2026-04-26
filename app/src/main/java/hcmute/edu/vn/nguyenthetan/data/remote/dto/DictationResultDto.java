package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DictationResultDto {
    @SerializedName("correct")
    public boolean correct;

    @SerializedName(value = "matchedCount", alternate = {"matched_count"})
    public int matchedCount;

    @SerializedName(value = "totalWords", alternate = {"total_words"})
    public int totalWords;

    @SerializedName(value = "hintWords", alternate = {"hint_words"})
    public List<String> hintWords;

    @SerializedName(value = "newHintIndex", alternate = {"new_hint_index"})
    public int newHintIndex;

    @SerializedName("hint")
    public String hint;

    @SerializedName(value = "correctSentence", alternate = {"correct_sentence"})
    public String correctSentence;

    @SerializedName("message")
    public String message;
}
