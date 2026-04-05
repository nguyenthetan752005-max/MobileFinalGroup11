package hcmute.edu.vn.nguyenthetan.data.local.entity.catalog;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "category_local",
        indices = {
                @Index(value = {"slug"}, unique = true)
        }
)
public class CategoryEntity {

    @PrimaryKey
    public long id;

    @NonNull
    public String slug;

    @NonNull
    public String name;

    public String imageUrl;
    public String levelRange;
    public String contentType;
    public String practiceType;
    public int totalLessons;
    public String description;
    public int orderIndex;

    public CategoryEntity(
            long id,
            @NonNull String slug,
            @NonNull String name,
            String imageUrl,
            String levelRange,
            String contentType,
            String practiceType,
            int totalLessons,
            String description,
            int orderIndex
    ) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.imageUrl = imageUrl;
        this.levelRange = levelRange;
        this.contentType = contentType;
        this.practiceType = practiceType;
        this.totalLessons = totalLessons;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}
