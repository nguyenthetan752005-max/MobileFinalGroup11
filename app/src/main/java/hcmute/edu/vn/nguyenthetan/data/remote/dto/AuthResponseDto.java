package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponseDto {
    @SerializedName("success")
    public boolean success;

    @SerializedName("code")
    public String code;

    @SerializedName("message")
    public String message;

    @SerializedName(value = "userId", alternate = {"user_id"})
    public Long userId;

    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName(value = "avatarUrl", alternate = {"avatar_url"})
    public String avatarUrl;

    @SerializedName("token")
    public String token;
}
