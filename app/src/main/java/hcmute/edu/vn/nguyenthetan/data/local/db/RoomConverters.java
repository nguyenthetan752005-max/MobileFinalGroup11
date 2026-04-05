package hcmute.edu.vn.nguyenthetan.data.local.db;

import androidx.room.TypeConverter;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;

public class RoomConverters {

    @TypeConverter
    public String sentenceStatusToValue(SentenceStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public SentenceStatus valueToSentenceStatus(String value) {
        return value == null ? SentenceStatus.NOT_STARTED : SentenceStatus.valueOf(value);
    }
}
