package hcmute.edu.vn.nguyenthetan.data.local.entity.community;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leaderboard_meta_local")
public class LeaderboardMetaEntity {

    @PrimaryKey
    public long id;

    public int currentUserRank;
    public String currentUserTime;

    public LeaderboardMetaEntity(long id, int currentUserRank, String currentUserTime) {
        this.id = id;
        this.currentUserRank = currentUserRank;
        this.currentUserTime = currentUserTime;
    }
}
