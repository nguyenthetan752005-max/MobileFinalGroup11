package com.english.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppSettingForm {

    @NotBlank(message = "Site name is required.")
    @Size(max = 150, message = "Site name must be at most 150 characters.")
    private String siteName;

    @NotBlank(message = "SEO meta description is required.")
    @Size(max = 500, message = "SEO meta description must be at most 500 characters.")
    private String seoMetaDescription;

    @NotNull(message = "Max recent users is required.")
    @Min(value = 1, message = "Max recent users must be at least 1.")
    @Max(value = 100, message = "Max recent users must be at most 100.")
    private Integer maxRecentUsersOnDashboard;

    @NotNull(message = "Speaking pass threshold is required.")
    @Min(value = 0, message = "Speaking pass threshold must be between 0 and 100.")
    @Max(value = 100, message = "Speaking pass threshold must be between 0 and 100.")
    private Integer speakingPassThreshold;

    private boolean allowUserRegistration;

    @NotNull(message = "Online timeout minutes is required.")
    @Min(value = 1, message = "Timeout must be at least 1 minute.")
    @Max(value = 1440, message = "Timeout must be at most 24 hours (1440 mins).")
    private Integer onlineTimeoutMinutes;

    private boolean dailyReminderEnabled;

    @NotBlank(message = "Daily reminder time is required.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Reminder time must be in HH:mm format.")
    private String dailyReminderTime;

    @NotBlank(message = "Daily reminder timezone is required.")
    @Size(max = 80, message = "Reminder timezone must be at most 80 characters.")
    private String dailyReminderTimezone;
}
