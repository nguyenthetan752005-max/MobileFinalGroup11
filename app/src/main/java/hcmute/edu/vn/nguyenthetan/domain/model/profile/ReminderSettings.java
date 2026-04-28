package hcmute.edu.vn.nguyenthetan.domain.model.profile;

import androidx.annotation.Nullable;

/**
 * Domain model for user reminder settings.
 * Decouples UI from remote DTO format.
 */
public final class ReminderSettings {
    public final boolean dailyReminderEnabled;
    @Nullable public final String dailyReminderTime;
    @Nullable public final String dailyReminderTimezone;

    public ReminderSettings(boolean dailyReminderEnabled,
                            @Nullable String dailyReminderTime,
                            @Nullable String dailyReminderTimezone) {
        this.dailyReminderEnabled = dailyReminderEnabled;
        this.dailyReminderTime = dailyReminderTime;
        this.dailyReminderTimezone = dailyReminderTimezone;
    }
}
