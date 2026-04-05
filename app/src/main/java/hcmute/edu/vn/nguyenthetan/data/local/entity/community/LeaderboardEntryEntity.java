package hcmute.edu.vn.nguyenthetan.data.local.entity.community;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "leaderboard_entry_local",
        primaryKeys = {"period", "rank"}
)
public class LeaderboardEntryEntity {

    @NonNull
    public String period;
    public int rank;
    public String name;
    public String avatarLabel;
    public String activeTime;
    public boolean currentUser;

    public LeaderboardEntryEntity(
            String period,
            int rank,
            String name,
            String avatarLabel,
            String activeTime,
            boolean currentUser
    ) {
        this.period = period;
        this.rank = rank;
        this.name = name;
        this.avatarLabel = avatarLabel;
        this.activeTime = activeTime;
        this.currentUser = currentUser;
    }
}
