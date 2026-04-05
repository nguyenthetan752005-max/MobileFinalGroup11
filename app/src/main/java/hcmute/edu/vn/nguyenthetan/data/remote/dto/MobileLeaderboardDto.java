package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import java.util.List;

public class MobileLeaderboardDto {
    public List<MobileLeaderboardEntryDto> weeklyEntries;
    public List<MobileLeaderboardEntryDto> monthlyEntries;
    public int currentUserRank;
    public String currentUserTime;
}
