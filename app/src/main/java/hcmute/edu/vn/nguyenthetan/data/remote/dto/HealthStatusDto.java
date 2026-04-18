package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class HealthStatusDto {
    @SerializedName("status")
    public String status;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("version")
    public String version;
}
