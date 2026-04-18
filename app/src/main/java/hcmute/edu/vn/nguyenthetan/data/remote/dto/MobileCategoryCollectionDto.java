package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MobileCategoryCollectionDto {
    @SerializedName(value = "categoryId", alternate = {"category_id"})
    public String categoryId;

    @SerializedName(value = "categorySlug", alternate = {"category_slug"})
    public String categorySlug;

    @SerializedName(value = "categoryName", alternate = {"category_name"})
    public String categoryName;

    @SerializedName("description")
    public String description;

    @SerializedName(value = "totalLessons", alternate = {"total_lessons"})
    public int totalLessons;

    @SerializedName("sections")
    public List<MobileCategoryCollectionSectionDto> sections;
}
