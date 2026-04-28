package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.SpeakingResultDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingEvaluation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Uploads a recorded audio file for server-side speaking evaluation. Returns null
 * on any failure; the caller is responsible for showing a user-facing error.
 *
 * The audio file passed in is owned by the caller and is NOT deleted by this
 * use-case so the caller can decide retention based on result handling.
 */
public class EvaluateSpeakingUseCase {

    private static final MediaType AUDIO_MEDIA_TYPE = MediaType.parse("audio/wav");
    private static final MediaType TEXT_MEDIA_TYPE = MediaType.parse("text/plain");

    private final MobileApiService api;

    public EvaluateSpeakingUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public SpeakingEvaluation execute(File audioFile, String reference, long sentenceId) {
        try {
            RequestBody audioRequest = RequestBody.create(AUDIO_MEDIA_TYPE, audioFile);
            MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), audioRequest);
            RequestBody referenceText = RequestBody.create(TEXT_MEDIA_TYPE, reference == null ? "" : reference);
            RequestBody sentenceIdPart = RequestBody.create(TEXT_MEDIA_TYPE, String.valueOf(sentenceId));

            Response<SpeakingResultDto> response = api.evaluateSpeaking(audioPart, referenceText, sentenceIdPart).execute();
            if (response.isSuccessful() && response.body() != null) {
                return mapToEvaluation(response.body());
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private SpeakingEvaluation mapToEvaluation(SpeakingResultDto dto) {
        SpeakingAttempt best = null;
        if (dto.bestResult != null) {
            best = new SpeakingAttempt(
                    dto.bestResult.score,
                    dto.bestResult.recognizedText == null ? "" : dto.bestResult.recognizedText,
                    dto.bestResult.feedback == null ? "" : dto.bestResult.feedback,
                    dto.bestResult.audioUrl == null ? "" : dto.bestResult.audioUrl
            );
        }
        return new SpeakingEvaluation(dto.score, dto.accuracy,
                dto.recognizedText, dto.feedback, dto.audioUrl,
                dto.resultType, best);
    }
}
