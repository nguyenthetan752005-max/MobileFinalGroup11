package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class GenericApiResponseDto {
    @SerializedName("success")
    public boolean success;

    @SerializedName("code")
    public String code;

    @SerializedName("message")
    public String message;

    @SerializedName(value = "avatarUrl", alternate = {"avatar_url"})
    public String avatarUrl;
}
