package hcmute.edu.vn.nguyenthetan.data.local.entity.catalog;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recommendation_local")
public class RecommendationEntity {

    @PrimaryKey
    public long id;

    public String title;
    public String level;
    public int lessonCount;
    public String practiceType;
    public int orderIndex;

    public RecommendationEntity(long id, String title, String level, int lessonCount, String practiceType, int orderIndex) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.lessonCount = lessonCount;
        this.practiceType = practiceType;
        this.orderIndex = orderIndex;
    }
}
