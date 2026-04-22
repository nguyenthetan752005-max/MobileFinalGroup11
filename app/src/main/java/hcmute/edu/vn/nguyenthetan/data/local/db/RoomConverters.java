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

    @TypeConverter
    public String stringListToValue(java.util.List<String> list) {
        if (list == null) return null;
        return new com.google.gson.Gson().toJson(list);
    }

    @TypeConverter
    public java.util.List<String> valueToStringList(String value) {
        if (value == null) return null;
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<String>>(){}.getType();
        return new com.google.gson.Gson().fromJson(value, type);
    }
}
