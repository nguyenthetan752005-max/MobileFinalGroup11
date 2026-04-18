package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SpeakingResultDto {
    @SerializedName("score")
    public int score;

    @SerializedName("accuracy")
    public double accuracy;

    @SerializedName(value = "recognizedText", alternate = {"recognized_text", "transcribedText"})
    public String recognizedText;

    @SerializedName("feedback")
    public String feedback;

    @SerializedName(value = "audioUrl", alternate = {"audio_url"})
    public String audioUrl;

    @SerializedName(value = "resultType", alternate = {"result_type"})
    public String resultType;

    @SerializedName(value = "bestResult", alternate = {"best_result"})
    public BestResultDto bestResult;

    public static class BestResultDto {
        @SerializedName("score")
        public int score;
        
        @SerializedName(value = "recognizedText", alternate = {"recognized_text", "transcribedText"})
        public String recognizedText;
        
        @SerializedName("feedback")
        public String feedback;
        
        @SerializedName(value = "audioUrl", alternate = {"audio_url"})
        public String audioUrl;
    }
}
