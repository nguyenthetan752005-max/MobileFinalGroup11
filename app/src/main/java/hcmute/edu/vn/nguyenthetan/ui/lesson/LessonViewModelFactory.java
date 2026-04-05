package hcmute.edu.vn.nguyenthetan.ui.lesson;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;

public class LessonViewModelFactory implements ViewModelProvider.Factory {

    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final long lessonId;

    public LessonViewModelFactory(
            GetLessonSessionUseCase getLessonSessionUseCase,
            GetLessonProgressUseCase getLessonProgressUseCase,
            GetCommentsUseCase getCommentsUseCase,
            CheckDictationAnswerUseCase checkDictationAnswerUseCase,
            SaveSentenceStatusUseCase saveSentenceStatusUseCase,
            SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase,
            long lessonId
    ) {
        this.getLessonSessionUseCase = getLessonSessionUseCase;
        this.getLessonProgressUseCase = getLessonProgressUseCase;
        this.getCommentsUseCase = getCommentsUseCase;
        this.checkDictationAnswerUseCase = checkDictationAnswerUseCase;
        this.saveSentenceStatusUseCase = saveSentenceStatusUseCase;
        this.saveSpeakingAttemptUseCase = saveSpeakingAttemptUseCase;
        this.lessonId = lessonId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LessonViewModel.class)) {
            return (T) new LessonViewModel(
                    getLessonSessionUseCase,
                    getLessonProgressUseCase,
                    getCommentsUseCase,
                    checkDictationAnswerUseCase,
                    saveSentenceStatusUseCase,
                    saveSpeakingAttemptUseCase,
                    lessonId
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
