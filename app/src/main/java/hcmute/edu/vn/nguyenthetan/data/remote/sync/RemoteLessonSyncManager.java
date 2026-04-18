package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLessonDetailDto;
import hcmute.edu.vn.nguyenthetan.data.remote.mapper.RemoteEntityMapper;
import retrofit2.Response;

public class RemoteLessonSyncManager {

    private static final String TAG = "RemoteLessonSync";

    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;

    public RemoteLessonSyncManager(MobileApiService mobileApiService, TungTungDatabase database) {
        this.mobileApiService = mobileApiService;
        this.database = database;
    }

    public void syncLesson(long lessonId) {
        try {
            Response<MobileLessonDetailDto> response = RetryUtil.retryWithBackoff(
                    () -> mobileApiService.getLessonDetail(lessonId).execute(),
                    3,
                    700L,
                    2500L,
                    2.0
            );
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Lesson detail sync skipped. HTTP " + response.code() + " for lesson " + lessonId);
                return;
            }

            MobileLessonDetailDto lessonDetail = response.body();
            LessonEntity lessonEntity = RemoteEntityMapper.toLessonEntity(lessonDetail);
            List<SentenceEntity> sentences = RemoteEntityMapper.toSentenceEntities(lessonDetail.sentences);
            if (lessonEntity == null || sentences.isEmpty()) {
                Log.w(TAG, "Lesson detail sync skipped. Empty payload for lesson " + lessonId);
                return;
            }

            database.runInTransaction(() -> {
                database.lessonDao().insertAll(java.util.Collections.singletonList(lessonEntity));
                database.sentenceDao().deleteByLessonId(lessonId);
                database.sentenceDao().insertAll(sentences);
            });
        } catch (IOException exception) {
            Log.w(TAG, "Lesson detail sync failed for lesson " + lessonId + ": " + exception.getMessage());
        }
    }
}
