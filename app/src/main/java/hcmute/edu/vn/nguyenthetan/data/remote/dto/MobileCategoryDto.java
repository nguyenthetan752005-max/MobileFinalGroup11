package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileCategoryDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName("slug")
    public String slug;

    @SerializedName("name")
    public String name;

    @SerializedName(value = "imageUrl", alternate = {"image_url"})
    public String imageUrl;

    @SerializedName(value = "levelRange", alternate = {"level_range"})
    public String levelRange;

    @SerializedName(value = "contentType", alternate = {"content_type", "type"})
    public String contentType;

    @SerializedName(value = "practiceType", alternate = {"practice_type"})
    public String practiceType;

    @SerializedName(value = "totalLessons", alternate = {"total_lessons"})
    public int totalLessons;

    @SerializedName("description")
    public String description;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;
}
