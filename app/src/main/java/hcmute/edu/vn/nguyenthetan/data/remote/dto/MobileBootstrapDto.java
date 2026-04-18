package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileBootstrapDto {
    @SerializedName(value = "version", alternate = {"catalogVersion"})
    public String version;

    @SerializedName(value = "generatedAt", alternate = {"generated_at"})
    public String generatedAt;

    @SerializedName(value = "categories", alternate = {"categoryList"})
    public List<MobileCategoryDto> categories;

    @SerializedName(value = "sections", alternate = {"sectionList"})
    public List<MobileSectionDto> sections;

    @SerializedName(value = "lessons", alternate = {"lessonList"})
    public List<MobileLessonDto> lessons;

    @SerializedName(value = "sentences", alternate = {"sentenceList"})
    public List<MobileSentenceDto> sentences;

    @SerializedName(value = "comments", alternate = {"commentList"})
    public List<MobileBootstrapCommentDto> comments;

    @SerializedName("leaderboard")
    public MobileLeaderboardDto leaderboard;
}
