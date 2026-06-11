package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileLeaderboardResponse {
    private List<MobileLeaderboardEntryResponse> weeklyEntries;
    private List<MobileLeaderboardEntryResponse> monthlyEntries;
    private Integer currentUserRank;
    private String currentUserTime;
}
