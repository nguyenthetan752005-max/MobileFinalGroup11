package hcmute.edu.vn.nguyenthetan.data.local.entity.user;

import androidx.room.Entity;

@Entity(
        tableName = "daily_activity_local",
        primaryKeys = {"dayIndex"}
)
public class DailyActivityEntity {

    public int dayIndex;
    public String dayLabel;
    public int minutes;
    public boolean highlighted;

    public DailyActivityEntity(int dayIndex, String dayLabel, int minutes, boolean highlighted) {
        this.dayIndex = dayIndex;
        this.dayLabel = dayLabel;
        this.minutes = minutes;
        this.highlighted = highlighted;
    }
}
