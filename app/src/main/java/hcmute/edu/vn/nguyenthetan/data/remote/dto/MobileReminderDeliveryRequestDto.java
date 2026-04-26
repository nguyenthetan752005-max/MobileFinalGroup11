package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class MobileReminderDeliveryRequestDto {
    public final String title;
    public final String body;
    public final String meta;
    public final String reminderDate;
    public final String reminderTime;
    public final String reminderTimezone;
    public final long deliveredAtEpochMillis;
    public final Long lessonId;
    public final String lessonTitle;

    public MobileReminderDeliveryRequestDto(
            String title,
            String body,
            String meta,
            String reminderDate,
            String reminderTime,
            String reminderTimezone,
            long deliveredAtEpochMillis,
            Long lessonId,
            String lessonTitle
    ) {
        this.title = title;
        this.body = body;
        this.meta = meta;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.reminderTimezone = reminderTimezone;
        this.deliveredAtEpochMillis = deliveredAtEpochMillis;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
    }
}
