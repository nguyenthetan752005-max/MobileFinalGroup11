package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileLeaderboardDto {
    @SerializedName(value = "weeklyEntries", alternate = {"weekly_entries"})
    public List<MobileLeaderboardEntryDto> weeklyEntries;

    @SerializedName(value = "monthlyEntries", alternate = {"monthly_entries"})
    public List<MobileLeaderboardEntryDto> monthlyEntries;

    @SerializedName(value = "currentUserRank", alternate = {"current_user_rank"})
    public int currentUserRank;

    @SerializedName(value = "currentUserTime", alternate = {"current_user_time"})
    public String currentUserTime;
}
