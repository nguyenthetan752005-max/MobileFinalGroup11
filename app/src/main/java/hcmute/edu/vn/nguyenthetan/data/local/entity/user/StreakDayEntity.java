package hcmute.edu.vn.nguyenthetan.data.local.entity.user;

import androidx.room.Entity;

@Entity(
        tableName = "streak_day_local",
        primaryKeys = {"dayIndex"}
)
public class StreakDayEntity {

    public int dayIndex;
    public boolean studied;
    public boolean today;

    public StreakDayEntity(int dayIndex, boolean studied, boolean today) {
        this.dayIndex = dayIndex;
        this.studied = studied;
        this.today = today;
    }
}
