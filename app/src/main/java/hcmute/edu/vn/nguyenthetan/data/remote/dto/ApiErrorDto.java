package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ApiErrorDto {
    @SerializedName("success")
    public boolean success;

    @SerializedName("code")
    public String code;

    @SerializedName("message")
    public String message;
}
