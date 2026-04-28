package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import androidx.annotation.Nullable;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.CheckDictationRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.DictationResultDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationResult;
import retrofit2.Response;

/**
 * Calls the server's dictation check endpoint. Returns null on any network/HTTP
 * failure so callers can fall back to the offline {@link CheckDictationAnswerUseCase}.
 */
public class CheckDictationOnlineUseCase {

    private final MobileApiService api;

    public CheckDictationOnlineUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public DictationResult execute(long sentenceId, String input) {
        try {
            Response<DictationResultDto> response = api.checkDictation(
                    new CheckDictationRequestDto(sentenceId, input)
            ).execute();
            if (response.isSuccessful() && response.body() != null) {
                DictationResultDto dto = response.body();
                return new DictationResult(dto.correct, dto.matchedCount, dto.totalWords,
                        dto.hintWords, dto.newHintIndex, dto.hint,
                        dto.correctSentence, dto.message);
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
