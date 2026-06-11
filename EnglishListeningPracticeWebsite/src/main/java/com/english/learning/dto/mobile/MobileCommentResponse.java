package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileCommentResponse {
    private Long id;
    private Long sentenceId;
    private Long parentCommentId;
    private Long userId;
    private String author;
    private String avatarLabel;
    private String timeAgo;
    private String content;
    private Long likeCount;
    private Long dislikeCount;
    private Integer orderIndex;
}
