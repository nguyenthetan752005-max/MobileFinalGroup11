package hcmute.edu.vn.nguyenthetan.data.local.entity.user;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings_local")
public class AppSettingsEntity {

    @PrimaryKey
    public long id;

    public String languageCode;
    public boolean onboardingCompleted;
    public Long lastOpenedLessonId;
    public Long lastOpenedSentenceId;
    public String lastViewedCategorySlug;

    public AppSettingsEntity(
            long id,
            String languageCode,
            boolean onboardingCompleted,
            Long lastOpenedLessonId,
            Long lastOpenedSentenceId,
            String lastViewedCategorySlug
    ) {
        this.id = id;
        this.languageCode = languageCode;
        this.onboardingCompleted = onboardingCompleted;
        this.lastOpenedLessonId = lastOpenedLessonId;
        this.lastOpenedSentenceId = lastOpenedSentenceId;
        this.lastViewedCategorySlug = lastViewedCategorySlug;
    }
}
