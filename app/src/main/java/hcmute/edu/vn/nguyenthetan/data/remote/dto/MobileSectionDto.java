package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileSectionDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName(value = "categoryId", alternate = {"category_id"})
    public String categoryId;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;
}
