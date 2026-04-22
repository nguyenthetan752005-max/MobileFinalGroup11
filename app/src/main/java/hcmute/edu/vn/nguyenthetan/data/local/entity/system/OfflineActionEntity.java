package hcmute.edu.vn.nguyenthetan.data.local.entity.system;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_actions")
public class OfflineActionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String actionType;
    public String payloadJson;
    public String localFilePath; // for audio files if any
    public long createdAt;

    public OfflineActionEntity(String actionType, String payloadJson, String localFilePath, long createdAt) {
        this.actionType = actionType;
        this.payloadJson = payloadJson;
        this.localFilePath = localFilePath;
        this.createdAt = createdAt;
    }
}
