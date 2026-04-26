package hcmute.edu.vn.nguyenthetan.ui.lesson;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.SyncSentenceCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;

public class LessonViewModelFactory implements ViewModelProvider.Factory {

    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final SyncLessonProgressUseCase syncLessonProgressUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final SyncSentenceCommentsUseCase syncSentenceCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final long lessonId;
    private final boolean loggedIn;

    public LessonViewModelFactory(
            GetLessonSessionUseCase getLessonSessionUseCase,
            GetLessonProgressUseCase getLessonProgressUseCase,
            SyncLessonProgressUseCase syncLessonProgressUseCase,
            GetCommentsUseCase getCommentsUseCase,
            SyncSentenceCommentsUseCase syncSentenceCommentsUseCase,
            CheckDictationAnswerUseCase checkDictationAnswerUseCase,
            SaveSentenceStatusUseCase saveSentenceStatusUseCase,
            SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase,
            long lessonId,
            boolean loggedIn
    ) {
        this.getLessonSessionUseCase = getLessonSessionUseCase;
        this.getLessonProgressUseCase = getLessonProgressUseCase;
        this.syncLessonProgressUseCase = syncLessonProgressUseCase;
        this.getCommentsUseCase = getCommentsUseCase;
        this.syncSentenceCommentsUseCase = syncSentenceCommentsUseCase;
        this.checkDictationAnswerUseCase = checkDictationAnswerUseCase;
        this.saveSentenceStatusUseCase = saveSentenceStatusUseCase;
        this.saveSpeakingAttemptUseCase = saveSpeakingAttemptUseCase;
        this.lessonId = lessonId;
        this.loggedIn = loggedIn;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LessonViewModel.class)) {
            return (T) new LessonViewModel(
                    getLessonSessionUseCase,
                    getLessonProgressUseCase,
                    syncLessonProgressUseCase,
                    getCommentsUseCase,
                    syncSentenceCommentsUseCase,
                    checkDictationAnswerUseCase,
                    saveSentenceStatusUseCase,
                    saveSpeakingAttemptUseCase,
                    lessonId,
                    loggedIn
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
