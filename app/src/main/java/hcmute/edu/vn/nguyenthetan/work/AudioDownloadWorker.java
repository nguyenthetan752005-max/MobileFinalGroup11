package hcmute.edu.vn.nguyenthetan.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.SentenceDao;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AudioDownloadWorker extends Worker {

    private static final String TAG = "AudioDownloadWorker";
    private static final String INPUT_LESSON_ID = "lesson_id";

    public AudioDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueue(@NonNull Context context, long lessonId) {
        Data inputData = new Data.Builder()
                .putLong(INPUT_LESSON_ID, lessonId)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(AudioDownloadWorker.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(uniqueWorkName(lessonId), ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        long lessonId = getInputData().getLong(INPUT_LESSON_ID, 0L);
        if (lessonId <= 0L) {
            return Result.failure();
        }

        TungTungDatabase database = Room.databaseBuilder(
                        getApplicationContext(),
                        TungTungDatabase.class,
                        "tungtung.db"
                )
                .fallbackToDestructiveMigration()
                .build();

        try {
            SentenceDao sentenceDao = database.sentenceDao();
            List<SentenceEntity> sentences = sentenceDao.getByLessonId(lessonId);
            OkHttpClient client = new OkHttpClient();
            File cacheDir = new File(getApplicationContext().getFilesDir(), "audio_cache");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                return Result.retry();
            }

            for (SentenceEntity sentence : sentences) {
                if (sentence == null || sentence.audioUrl == null || sentence.audioUrl.trim().isEmpty()) {
                    continue;
                }
                File targetFile = new File(cacheDir, "audio_" + sentence.id + ".mp3");
                if (targetFile.exists() && targetFile.isFile() && targetFile.length() > 0L) {
                    sentenceDao.updateLocalAudioPath(sentence.id, targetFile.getAbsolutePath());
                    continue;
                }

                Request request = new Request.Builder()
                        .url(sentence.audioUrl)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.w(TAG, "Audio download failed. HTTP " + response.code() + " for sentence " + sentence.id);
                        if (response.code() >= 500) {
                            return Result.retry();
                        }
                        continue;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        continue;
                    }

                    try (InputStream inputStream = body.byteStream();
                         FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }

                    if (targetFile.length() > 0L) {
                        sentenceDao.updateLocalAudioPath(sentence.id, targetFile.getAbsolutePath());
                    }
                } catch (IOException exception) {
                    Log.w(TAG, "Audio download failed for sentence " + sentence.id + ": " + exception.getMessage());
                    return Result.retry();
                }
            }

            return Result.success();
        } finally {
            database.close();
        }
    }

    private static String uniqueWorkName(long lessonId) {
        return "lesson-audio-download-" + lessonId;
    }
}
