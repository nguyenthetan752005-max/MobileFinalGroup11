package hcmute.edu.vn.nguyenthetan.data.local.entity.lesson;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sentence_local",
        foreignKeys = {
                @ForeignKey(
                        entity = LessonEntity.class,
                        parentColumns = "id",
                        childColumns = "lessonId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("lessonId")
        }
)
public class SentenceEntity {

    @PrimaryKey
    public long id;

    public long lessonId;
    public String audioUrl;
    public String localAudioPath;
    public String content;
    public String hintText;
    public long durationMillis;
    public Double startTime;
    public Double endTime;
    public int orderIndex;

    public SentenceEntity(
            long id,
            long lessonId,
            String audioUrl,
            String localAudioPath,
            String content,
            String hintText,
            long durationMillis,
            Double startTime,
            Double endTime,
            int orderIndex
    ) {
        this.id = id;
        this.lessonId = lessonId;
        this.audioUrl = audioUrl;
        this.localAudioPath = localAudioPath;
        this.content = content;
        this.hintText = hintText;
        this.durationMillis = durationMillis;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderIndex = orderIndex;
    }
}
