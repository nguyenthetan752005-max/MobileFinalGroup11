package hcmute.edu.vn.nguyenthetan.data.local.entity.lesson;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;


@Entity(
        tableName = "lesson_local",
        foreignKeys = {
                @ForeignKey(
                        entity = SectionEntity.class,
                        parentColumns = "id",
                        childColumns = "sectionId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("sectionId")
        }
)
public class LessonEntity {

    @PrimaryKey
    public long id;

    public long sectionId;
    public String title;
    public String level;
    public String practiceType;
    public String contentType;
    public int totalSentences;
    public int passThreshold;
    public String youtubeVideoId;
    public int orderIndex;

    public LessonEntity(
            long id,
            long sectionId,
            String title,
            String level,
            String practiceType,
            String contentType,
            int totalSentences,
            int passThreshold,
            String youtubeVideoId,
            int orderIndex
    ) {
        this.id = id;
        this.sectionId = sectionId;
        this.title = title;
        this.level = level;
        this.practiceType = practiceType;
        this.contentType = contentType;
        this.totalSentences = totalSentences;
        this.passThreshold = passThreshold;
        this.youtubeVideoId = youtubeVideoId;
        this.orderIndex = orderIndex;
    }
}
