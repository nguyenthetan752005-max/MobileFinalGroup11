package hcmute.edu.vn.nguyenthetan.data.local.entity.system;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_state_local")
public class SyncStateEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String version;
    public long lastSyncAt;

    public SyncStateEntity(@NonNull String key, String version, long lastSyncAt) {
        this.key = key;
        this.version = version;
        this.lastSyncAt = lastSyncAt;
    }
}
