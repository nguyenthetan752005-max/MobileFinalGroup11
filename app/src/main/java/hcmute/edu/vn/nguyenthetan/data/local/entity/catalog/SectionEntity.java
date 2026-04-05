package hcmute.edu.vn.nguyenthetan.data.local.entity.catalog;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "section_local",
        foreignKeys = {
                @ForeignKey(
                        entity = CategoryEntity.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("categoryId")
        }
)
public class SectionEntity {

    @PrimaryKey
    public long id;

    public long categoryId;
    public String name;
    public String description;
    public int orderIndex;

    public SectionEntity(long id, long categoryId, String name, String description, int orderIndex) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}
