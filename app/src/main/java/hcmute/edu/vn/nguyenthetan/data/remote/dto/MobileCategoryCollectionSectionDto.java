package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileCategoryCollectionSectionDto {
    @SerializedName(value = "id", alternate = {"_id"})
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName(value = "orderIndex", alternate = {"order_index"})
    public int orderIndex;

    @SerializedName("lessons")
    public List<MobileLessonDto> lessons;
}
