package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import androidx.annotation.Nullable;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.DictationResultDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.ProgressUpdateRequestDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationResult;
import retrofit2.Response;

public class SkipDictationUseCase {

    private final MobileApiService api;

    public SkipDictationUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public DictationResult execute(long userId, long sentenceId) {
        try {
            Response<DictationResultDto> response = api.skipDictation(
                    new ProgressUpdateRequestDto(userId, sentenceId)
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
