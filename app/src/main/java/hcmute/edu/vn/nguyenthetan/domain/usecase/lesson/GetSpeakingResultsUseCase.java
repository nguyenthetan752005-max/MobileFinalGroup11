package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import androidx.annotation.Nullable;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.SpeakingResultDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingEvaluation;
import retrofit2.Response;

public class GetSpeakingResultsUseCase {

    private final MobileApiService api;

    public GetSpeakingResultsUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public SpeakingEvaluation execute(long sentenceId) {
        try {
            Response<SpeakingResultDto> response = api.getSpeakingResults(sentenceId).execute();
            if (response.isSuccessful() && response.body() != null) {
                SpeakingResultDto dto = response.body();
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
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
