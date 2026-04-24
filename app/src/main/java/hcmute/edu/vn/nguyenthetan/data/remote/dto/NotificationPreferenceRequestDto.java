package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class NotificationPreferenceRequestDto {
    @SerializedName("notificationsEnabled")
    public boolean notificationsEnabled;

    @SerializedName("timezone")
    public String timezone;

    public NotificationPreferenceRequestDto(boolean notificationsEnabled, String timezone) {
        this.notificationsEnabled = notificationsEnabled;
        this.timezone = timezone;
    }
}
