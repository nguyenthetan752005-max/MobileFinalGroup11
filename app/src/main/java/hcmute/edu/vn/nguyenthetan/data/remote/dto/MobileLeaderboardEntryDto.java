package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileLeaderboardEntryDto {
    @SerializedName("rank")
    public int rank;

    @SerializedName("name")
    public String name;

    @SerializedName(value = "avatarLabel", alternate = {"avatar_label"})
    public String avatarLabel;

    @SerializedName(value = "activeTime", alternate = {"active_time"})
    public String activeTime;

    @SerializedName(value = "currentUser", alternate = {"current_user"})
    public boolean currentUser;
}
