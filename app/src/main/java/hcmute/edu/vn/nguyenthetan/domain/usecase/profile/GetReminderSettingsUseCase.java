package hcmute.edu.vn.nguyenthetan.domain.usecase.profile;

import androidx.annotation.Nullable;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileReminderSettingsDto;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ReminderSettings;
import retrofit2.Response;

/**
 * Fetches the user's daily reminder settings. Returns null on any failure so
 * callers can fall back to local defaults without needing to interpret HTTP errors.
 */
public class GetReminderSettingsUseCase {

    private final MobileApiService api;

    public GetReminderSettingsUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public ReminderSettings execute() {
        try {
            Response<MobileReminderSettingsDto> response = api.getReminderSettings().execute();
            if (response.isSuccessful() && response.body() != null) {
                MobileReminderSettingsDto dto = response.body();
                return new ReminderSettings(dto.dailyReminderEnabled,
                        dto.dailyReminderTime, dto.dailyReminderTimezone);
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
