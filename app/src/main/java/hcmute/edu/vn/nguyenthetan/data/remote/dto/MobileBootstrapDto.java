package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import java.util.List;

public class MobileBootstrapDto {
    public String version;
    public String generatedAt;
    public List<MobileCategoryDto> categories;
    public List<MobileSectionDto> sections;
    public List<MobileLessonDto> lessons;
    public List<MobileSentenceDto> sentences;
    public List<MobileBootstrapCommentDto> comments;
    public MobileLeaderboardDto leaderboard;
}
