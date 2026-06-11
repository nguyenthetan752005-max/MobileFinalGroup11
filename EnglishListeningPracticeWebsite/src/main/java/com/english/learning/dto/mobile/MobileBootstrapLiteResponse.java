package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Bootstrap Lite response: catalog nhẹ cho home/explore.
 * Không trả sentences → Android sẽ gọi /api/mobile/lessons/{id} khi cần.
 */
@Data
@Builder
public class MobileBootstrapLiteResponse {
    private String version;
    private String generatedAt;
    private List<MobileCategoryResponse> categories;
    private List<MobileSectionResponse> sections;
    private List<MobileLessonResponse> lessons;
    private List<MobileCommentResponse> comments;
    private MobileLeaderboardResponse leaderboard;
}
