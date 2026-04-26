package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileNotificationSummaryDto {
    @SerializedName(value = "unreadCount", alternate = {"unread_count"})
    public long unreadCount;
}
