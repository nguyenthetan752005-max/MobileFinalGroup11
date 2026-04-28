package hcmute.edu.vn.nguyenthetan.ui.lesson;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.AddCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.DeleteCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.SyncSentenceCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.VoteCommentUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationOnlineUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.EvaluateSpeakingUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetSpeakingResultsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SkipDictationUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SyncLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.TrackLessonTimeUseCase;

public class LessonViewModelFactory implements ViewModelProvider.Factory {

    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final SyncLessonProgressUseCase syncLessonProgressUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final SyncSentenceCommentsUseCase syncSentenceCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final CheckDictationOnlineUseCase checkDictationOnlineUseCase;
    private final SkipDictationUseCase skipDictationUseCase;
    private final EvaluateSpeakingUseCase evaluateSpeakingUseCase;
    private final GetSpeakingResultsUseCase getSpeakingResultsUseCase;
    private final TrackLessonTimeUseCase trackLessonTimeUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final VoteCommentUseCase voteCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final UserSessionStore session;
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
            CheckDictationOnlineUseCase checkDictationOnlineUseCase,
            SkipDictationUseCase skipDictationUseCase,
            EvaluateSpeakingUseCase evaluateSpeakingUseCase,
            GetSpeakingResultsUseCase getSpeakingResultsUseCase,
            TrackLessonTimeUseCase trackLessonTimeUseCase,
            AddCommentUseCase addCommentUseCase,
            VoteCommentUseCase voteCommentUseCase,
            DeleteCommentUseCase deleteCommentUseCase,
            UserSessionStore session,
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
        this.checkDictationOnlineUseCase = checkDictationOnlineUseCase;
        this.skipDictationUseCase = skipDictationUseCase;
        this.evaluateSpeakingUseCase = evaluateSpeakingUseCase;
        this.getSpeakingResultsUseCase = getSpeakingResultsUseCase;
        this.trackLessonTimeUseCase = trackLessonTimeUseCase;
        this.addCommentUseCase = addCommentUseCase;
        this.voteCommentUseCase = voteCommentUseCase;
        this.deleteCommentUseCase = deleteCommentUseCase;
        this.session = session;
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
                    checkDictationOnlineUseCase,
                    skipDictationUseCase,
                    evaluateSpeakingUseCase,
                    getSpeakingResultsUseCase,
                    trackLessonTimeUseCase,
                    addCommentUseCase,
                    voteCommentUseCase,
                    deleteCommentUseCase,
                    session,
                    lessonId,
                    loggedIn
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
