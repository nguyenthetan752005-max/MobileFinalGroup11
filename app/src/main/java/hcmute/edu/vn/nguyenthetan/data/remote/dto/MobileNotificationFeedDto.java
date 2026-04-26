package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileNotificationFeedDto {
    @SerializedName(value = "unreadCount", alternate = {"unread_count"})
    public long unreadCount;

    @SerializedName("items")
    public List<MobileNotificationItemDto> items;
}
