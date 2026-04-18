package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {
    @SerializedName("id")
    public long id;

    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName(value = "avatarUrl", alternate = {"avatar_url"})
    public String avatarUrl;

    @SerializedName(value = "totalActiveTime", alternate = {"total_active_time"})
    public int totalActiveTime;

    @SerializedName(value = "formattedActiveTime", alternate = {"formatted_active_time"})
    public String formattedActiveTime;

    @SerializedName(value = "activeTime7d", alternate = {"active_time_7d"})
    public int activeTime7d;

    @SerializedName(value = "activeTime30d", alternate = {"active_time_30d"})
    public int activeTime30d;
}
