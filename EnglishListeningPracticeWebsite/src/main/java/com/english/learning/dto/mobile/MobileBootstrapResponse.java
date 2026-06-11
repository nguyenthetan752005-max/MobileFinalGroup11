package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileBootstrapResponse {
    private String version;
    private String generatedAt;
    private List<MobileCategoryResponse> categories;
    private List<MobileSectionResponse> sections;
    private List<MobileLessonResponse> lessons;
    private List<MobileSentenceResponse> sentences;
    private List<MobileCommentResponse> comments;
    private MobileLeaderboardResponse leaderboard;
}
