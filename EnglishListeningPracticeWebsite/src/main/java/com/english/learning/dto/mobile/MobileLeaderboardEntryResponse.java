package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileLeaderboardEntryResponse {
    private Integer rank;
    private String name;
    private String avatarLabel;
    private String activeTime;
    private Boolean currentUser;
}
