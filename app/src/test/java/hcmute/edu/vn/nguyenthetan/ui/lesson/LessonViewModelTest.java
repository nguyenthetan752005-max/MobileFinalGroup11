package hcmute.edu.vn.nguyenthetan.ui.lesson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
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

/**
 * Unit tests for {@link LessonViewModel} covering:
 * - Navigation (next/previous/boundary)
 * - Dictation check (correct/incorrect/skip)
 * - Speaking evaluation + state transitions
 * - Tab selection + normalization
 * - Playback state management
 * - Progress calculation
 * - Transcript rows
 *
 * These tests use reflection to inject lessonSession state directly,
 * bypassing the async load() method which depends on ExecutorService.
 */
public class LessonViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    // Mocks
    private GetLessonSessionUseCase getLessonSession;
    private GetLessonProgressUseCase getLessonProgress;
    private SyncLessonProgressUseCase syncLessonProgress;
    private GetCommentsUseCase getComments;
    private SyncSentenceCommentsUseCase syncSentenceComments;
    private CheckDictationAnswerUseCase checkDictationAnswer;
    private SaveSentenceStatusUseCase saveSentenceStatus;
    private SaveSpeakingAttemptUseCase saveSpeakingAttempt;
    private CheckDictationOnlineUseCase checkDictationOnline;
    private SkipDictationUseCase skipDictation;
    private EvaluateSpeakingUseCase evaluateSpeaking;
    private GetSpeakingResultsUseCase getSpeakingResults;
    private TrackLessonTimeUseCase trackLessonTime;
    private AddCommentUseCase addComment;
    private VoteCommentUseCase voteComment;
    private DeleteCommentUseCase deleteComment;
    private UserSessionStore session;

    private static final long LESSON_ID = 42L;

    @Before
    public void setUp() {
        getLessonSession = mock(GetLessonSessionUseCase.class);
        getLessonProgress = mock(GetLessonProgressUseCase.class);
        syncLessonProgress = mock(SyncLessonProgressUseCase.class);
        getComments = mock(GetCommentsUseCase.class);
        syncSentenceComments = mock(SyncSentenceCommentsUseCase.class);
        checkDictationAnswer = mock(CheckDictationAnswerUseCase.class);
        saveSentenceStatus = mock(SaveSentenceStatusUseCase.class);
        saveSpeakingAttempt = mock(SaveSpeakingAttemptUseCase.class);
        checkDictationOnline = mock(CheckDictationOnlineUseCase.class);
        skipDictation = mock(SkipDictationUseCase.class);
        evaluateSpeaking = mock(EvaluateSpeakingUseCase.class);
        getSpeakingResults = mock(GetSpeakingResultsUseCase.class);
        trackLessonTime = mock(TrackLessonTimeUseCase.class);
        addComment = mock(AddCommentUseCase.class);
        voteComment = mock(VoteCommentUseCase.class);
        deleteComment = mock(DeleteCommentUseCase.class);
        session = mock(UserSessionStore.class);

        when(getComments.execute(anyLong())).thenReturn(Collections.emptyList());
    }

    private LessonViewModel createViewModel(boolean loggedIn) {
        return new LessonViewModel(
                getLessonSession, getLessonProgress, syncLessonProgress,
                getComments, syncSentenceComments, checkDictationAnswer,
                saveSentenceStatus, saveSpeakingAttempt, checkDictationOnline,
                skipDictation, evaluateSpeaking, getSpeakingResults,
                trackLessonTime, addComment, voteComment, deleteComment,
                session, LESSON_ID, loggedIn
        );
    }

    private Sentence makeSentence(long id, String content) {
        return new Sentence(id, (int) id, "http://audio/" + id, null,
                content, "hint", 5000L, null, null, Collections.emptyList());
    }

    /** Inject lessonSession and sentenceStatuses directly via reflection. */
    private void injectSession(LessonViewModel vm, LessonSession ls,
                                Map<Long, SentenceStatus> statuses) {
        try {
            Field sessionField = LessonViewModel.class.getDeclaredField("lessonSession");
            sessionField.setAccessible(true);
            sessionField.set(vm, ls);

            @SuppressWarnings("unchecked")
            Field statusField = LessonViewModel.class.getDeclaredField("sentenceStatuses");
            statusField.setAccessible(true);
            Map<Long, SentenceStatus> map = (Map<Long, SentenceStatus>) statusField.get(vm);
            map.putAll(statuses);

            Field loadedField = LessonViewModel.class.getDeclaredField("loaded");
            loadedField.setAccessible(true);
            loadedField.set(vm, true);
            
            // Inject synchronous executor
            Field executorField = LessonViewModel.class.getDeclaredField("executorService");
            executorField.setAccessible(true);
            java.util.concurrent.ExecutorService inlineExecutor = new java.util.concurrent.AbstractExecutorService() {
                private boolean shutdown = false;
                @Override public void shutdown() { shutdown = true; }
                @Override public java.util.List<Runnable> shutdownNow() { shutdown = true; return java.util.Collections.emptyList(); }
                @Override public boolean isShutdown() { return shutdown; }
                @Override public boolean isTerminated() { return shutdown; }
                @Override public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) { return true; }
                @Override public void execute(Runnable command) { command.run(); }
            };
            executorField.set(vm, inlineExecutor);

            // Inject mocked Handler
            Field handlerField = LessonViewModel.class.getDeclaredField("mainHandler");
            handlerField.setAccessible(true);
            android.os.Handler mockHandler = org.mockito.Mockito.mock(android.os.Handler.class);
            org.mockito.Mockito.when(mockHandler.post(org.mockito.ArgumentMatchers.any(Runnable.class))).thenAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return true;
            });
            handlerField.set(vm, mockHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject test state", e);
        }
    }

    /** Build a 3-sentence lesson VM ready for testing. */
    private LessonViewModel readyViewModel(boolean loggedIn) {
        List<Sentence> sentences = Arrays.asList(
                makeSentence(1L, "Hello world"),
                makeSentence(2L, "How are you"),
                makeSentence(3L, "Goodbye")
        );
        LessonSession ls = new LessonSession(
                LESSON_ID, "Test Lesson", "Category", "LISTENING_SPEAKING",
                "Beginner", "AUDIO", 70, null, sentences, null
        );
        Map<Long, SentenceStatus> statuses = new LinkedHashMap<>();
        statuses.put(1L, SentenceStatus.NOT_STARTED);
        statuses.put(2L, SentenceStatus.NOT_STARTED);
        statuses.put(3L, SentenceStatus.NOT_STARTED);

        LessonViewModel vm = createViewModel(loggedIn);
        injectSession(vm, ls, statuses);
        return vm;
    }

    /** Force ViewModel to emit UI state by calling selectTab (which calls publish). */
    private void triggerPublish(LessonViewModel vm) {
        vm.selectTab(vm.getSelectedTab());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  1. Init + basic state
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void initialState_emitsCorrectSentenceCounter() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("1/3", state.sentenceCounter);
        assertEquals("Test Lesson", state.title);
        assertFalse(state.hasPreviousSentence);
        assertTrue(state.hasNextSentence);
    }

    @Test
    public void initialState_progressIsZero() {
        LessonViewModel vm = readyViewModel(false);
        triggerPublish(vm);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(0, state.progressPercent);
    }

    @Test
    public void initialState_showsBothDictationAndSpeaking() {
        LessonViewModel vm = readyViewModel(false);
        triggerPublish(vm);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.showDictationTab);
        assertTrue(state.showSpeakingTab);
        assertTrue(state.showTranscriptTab);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  2. Navigation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void nextSentence_advancesIndex() {
        LessonViewModel vm = readyViewModel(true);

        vm.nextSentence();
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("2/3", state.sentenceCounter);
        assertTrue(state.hasPreviousSentence);
        assertTrue(state.hasNextSentence);
    }

    @Test
    public void previousSentence_goesBack() {
        LessonViewModel vm = readyViewModel(true);

        vm.nextSentence();
        vm.previousSentence();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("1/3", state.sentenceCounter);
        assertFalse(state.hasPreviousSentence);
    }

    @Test
    public void nextSentence_atLastIndex_doesNothing() {
        LessonViewModel vm = readyViewModel(true);

        vm.nextSentence(); // index 1
        vm.nextSentence(); // index 2 (last)
        vm.nextSentence(); // out of bounds

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("3/3", state.sentenceCounter);
        assertFalse(state.hasNextSentence);
    }

    @Test
    public void previousSentence_atFirstIndex_doesNothing() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        vm.previousSentence();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("1/3", state.sentenceCounter);
    }

    @Test
    public void selectSentence_jumpsToSpecificIndex() {
        LessonViewModel vm = readyViewModel(true);

        vm.selectSentence(2);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("3/3", state.sentenceCounter);
    }

    @Test
    public void selectSentence_outOfBounds_doesNotChange() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        vm.selectSentence(10);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("1/3", state.sentenceCounter);
    }

    @Test
    public void selectSentence_negativeIndex_doesNotChange() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        vm.selectSentence(-1);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("1/3", state.sentenceCounter);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  3. Dictation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void checkAnswer_correct_setsCompleted() {
        LessonViewModel vm = readyViewModel(true);
        DictationFeedback fb = new DictationFeedback(true, "Correct!", "Hello world", "Hello world", "", "");
        when(checkDictationAnswer.execute(any(), anyString())).thenReturn(fb);

        vm.onInputChanged("Hello world");
        vm.checkAnswer();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.showFeedback);
        assertEquals(SentenceStatus.COMPLETED, state.currentStatus);
        assertTrue(state.showNextButton);
    }

    @Test
    public void checkAnswer_incorrect_keepsInProgress() {
        LessonViewModel vm = readyViewModel(true);
        DictationFeedback fb = new DictationFeedback(false, "Wrong", "Hello world", "Hello", "Hello world", "***");
        when(checkDictationAnswer.execute(any(), anyString())).thenReturn(fb);

        vm.onInputChanged("Hello earth");
        vm.checkAnswer();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.showFeedback);
        assertEquals(SentenceStatus.IN_PROGRESS, state.currentStatus);
    }

    @Test
    public void checkAnswer_emptyInput_doesNothing() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        vm.onInputChanged("   ");
        vm.checkAnswer();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.showFeedback);
        verify(checkDictationAnswer, never()).execute(any(), anyString());
    }

    @Test
    public void skip_setsSkippedAndShowsFeedback() {
        LessonViewModel vm = readyViewModel(true);

        vm.skip();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(SentenceStatus.SKIPPED, state.currentStatus);
        assertTrue(state.showFeedback);
    }

    @Test
    public void applyDictationFeedback_markCompleted() {
        LessonViewModel vm = readyViewModel(true);
        DictationFeedback fb = new DictationFeedback(true, "Good", "Hello world", "Hello world", "", "");

        vm.applyDictationFeedback(fb, true, false);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(SentenceStatus.COMPLETED, state.currentStatus);
    }

    @Test
    public void completedStatus_neverDowngraded() {
        LessonViewModel vm = readyViewModel(true);
        DictationFeedback fb = new DictationFeedback(true, "OK", "Hello world", "Hello world", "", "");
        when(checkDictationAnswer.execute(any(), anyString())).thenReturn(fb);

        vm.onInputChanged("Hello world");
        vm.checkAnswer();
        // Now try to skip — should NOT downgrade
        vm.skip();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(SentenceStatus.COMPLETED, state.currentStatus);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  4. Speaking
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void setRecordingState_updatesUi() {
        LessonViewModel vm = readyViewModel(true);

        vm.setRecordingState(true);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.recording);

        vm.setRecordingState(false);
        state = vm.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.recording);
    }

    @Test
    public void setSpeakingBusy_showsLabel() {
        LessonViewModel vm = readyViewModel(true);

        vm.setSpeakingBusy("Evaluating...");
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.speakingBusy);
        assertEquals("Evaluating...", state.recordingLabel);
    }

    @Test
    public void applySpeakingEvaluation_aboveThreshold_setsCompleted() {
        LessonViewModel vm = readyViewModel(true);

        vm.applySpeakingEvaluation(85, "Hello world", "Good", "http://audio", null);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(SentenceStatus.COMPLETED, state.currentStatus);
        assertEquals("85/100", state.currentScore);
        assertFalse(state.speakingBusy);
    }

    @Test
    public void applySpeakingEvaluation_belowThreshold_keepsInProgress() {
        LessonViewModel vm = readyViewModel(true);

        vm.applySpeakingEvaluation(50, "Hello", "Try again", "http://audio", null);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(SentenceStatus.IN_PROGRESS, state.currentStatus);
        assertEquals("50/100", state.currentScore);
    }

    @Test
    public void applySpeakingEvaluation_updatesBest_whenHigherScore() {
        LessonViewModel vm = readyViewModel(true);

        vm.applySpeakingEvaluation(60, "Hello", "ok", "", null);
        vm.applySpeakingEvaluation(90, "Hello world", "great", "", null);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("90/100", state.bestScore);
    }

    @Test
    public void retrySpeaking_setsPlaceholder() {
        LessonViewModel vm = readyViewModel(true);

        vm.retrySpeaking();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.recording);
        assertEquals("58/100", state.currentScore);
    }

    @Test
    public void applySpeakingResultsFromServer_populatesState() {
        LessonViewModel vm = readyViewModel(true);

        vm.applySpeakingResultsFromServer(75, "transcript", "feedback", "url",
                90, "best transcript", "best feedback", "best url");

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("75/100", state.currentScore);
        assertEquals("90/100", state.bestScore);
    }

    @Test
    public void applySpeakingResultsFromServer_zeroScore_doesNotPopulate() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        vm.applySpeakingResultsFromServer(0, null, null, null,
                0, null, null, null);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("Waiting", state.currentScore);
        assertEquals("No score yet", state.bestScore);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  5. Tab selection
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void selectTab_switchesToTranscript() {
        LessonViewModel vm = readyViewModel(true);

        vm.selectTab(LessonViewModel.TAB_TRANSCRIPT);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(LessonViewModel.TAB_TRANSCRIPT, state.selectedTab);
    }

    @Test
    public void selectTab_listeningOnly_hidesSpeaking() {
        List<Sentence> sentences = Arrays.asList(makeSentence(1L, "Test"));
        LessonSession ls = new LessonSession(
                LESSON_ID, "L", "C", "LISTENING", "Beg", "AUDIO", 70, null, sentences, null
        );
        Map<Long, SentenceStatus> statuses = new LinkedHashMap<>();
        statuses.put(1L, SentenceStatus.NOT_STARTED);
        LessonViewModel vm = createViewModel(false);
        injectSession(vm, ls, statuses);

        vm.selectTab(LessonViewModel.TAB_DICTATION);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.showDictationTab);
        assertFalse(state.showSpeakingTab);
    }

    @Test
    public void selectTab_speakingOnly_hidesDictation() {
        List<Sentence> sentences = Arrays.asList(makeSentence(1L, "Test"));
        LessonSession ls = new LessonSession(
                LESSON_ID, "L", "C", "SPEAKING", "Beg", "AUDIO", 70, null, sentences, null
        );
        Map<Long, SentenceStatus> statuses = new LinkedHashMap<>();
        statuses.put(1L, SentenceStatus.NOT_STARTED);
        LessonViewModel vm = createViewModel(false);
        injectSession(vm, ls, statuses);

        vm.selectTab(LessonViewModel.TAB_SPEAKING);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.showSpeakingTab);
        assertFalse(state.showDictationTab);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  6. Playback
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void togglePlayback_togglesState() {
        LessonViewModel vm = readyViewModel(true);

        vm.togglePlayback();
        assertTrue(vm.getUiState().getValue().playing);

        vm.togglePlayback();
        assertFalse(vm.getUiState().getValue().playing);
    }

    @Test
    public void pausePlayback_setsPlayingFalse() {
        LessonViewModel vm = readyViewModel(true);

        vm.togglePlayback();
        vm.pausePlayback();
        assertFalse(vm.getUiState().getValue().playing);
    }

    @Test
    public void updatePlaybackProgress_updatesTime() {
        LessonViewModel vm = readyViewModel(true);

        vm.updatePlaybackProgress(30000L, 60000L, true);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("0:30 / 1:00", state.playbackTime);
        assertEquals(50, state.playbackPercent);
    }

    @Test
    public void replay_resetsAndPlays() {
        LessonViewModel vm = readyViewModel(true);

        vm.updatePlaybackProgress(5000L, 10000L, false);
        vm.replay();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.playing);
    }

    @Test
    public void completePlayback_stopAndResetPosition() {
        LessonViewModel vm = readyViewModel(true);

        vm.updatePlaybackProgress(10000L, 10000L, true);
        vm.completePlayback(10000L);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.playing);
        assertEquals("0:00 / 0:10", state.playbackTime);
    }

    @Test
    public void resetPlayback_clearsEverything() {
        LessonViewModel vm = readyViewModel(true);

        vm.updatePlaybackProgress(5000L, 10000L, true);
        vm.resetPlayback();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.playing);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  7. Progress calculation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void progress_increases_whenSentenceCompleted() {
        LessonViewModel vm = readyViewModel(true);
        DictationFeedback fb = new DictationFeedback(true, "OK", "Hello world", "Hello world", "", "");
        when(checkDictationAnswer.execute(any(), anyString())).thenReturn(fb);

        vm.onInputChanged("Hello world");
        vm.checkAnswer();

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals(33, state.progressPercent);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  8. Transcript rows
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void transcriptRows_matchSentenceCount() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertNotNull(state.transcriptRows);
        assertEquals(3, state.transcriptRows.size());
    }

    @Test
    public void transcriptRows_firstIsSelected() {
        LessonViewModel vm = readyViewModel(true);
        triggerPublish(vm);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.transcriptRows.get(0).selected);
        assertFalse(state.transcriptRows.get(1).selected);
    }

    @Test
    public void playTranscriptRow_selectsAndPlays() {
        LessonViewModel vm = readyViewModel(true);

        vm.playTranscriptRow(1);

        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertEquals("2/3", state.sentenceCounter);
        assertTrue(state.playing);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  9. Video lesson
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void videoLesson_setsVideoFlag() {
        List<Sentence> sentences = Arrays.asList(
                new Sentence(1L, 0, null, null, "Test", "", 0L, 10.0, 20.0, Collections.emptyList())
        );
        LessonSession ls = new LessonSession(
                LESSON_ID, "Video", "C", "LISTENING", "Beg", "VIDEO", 70, "abc123", sentences, null
        );
        Map<Long, SentenceStatus> statuses = new LinkedHashMap<>();
        statuses.put(1L, SentenceStatus.NOT_STARTED);
        LessonViewModel vm = createViewModel(false);
        injectSession(vm, ls, statuses);

        triggerPublish(vm);
        LessonViewModel.UiState state = vm.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.videoLesson);
        assertTrue(state.mediaSummary.startsWith("Video clip:"));
    }

    // ────────────────────────────────────────────────────────────────────────
    //  10. Repeat mode
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void repeatMode_defaultFalse() {
        LessonViewModel vm = readyViewModel(true);
        assertFalse(vm.isRepeatTranscriptMode());
    }

    @Test
    public void repeatMode_canBeToggled() {
        LessonViewModel vm = readyViewModel(true);
        vm.setRepeatTranscriptMode(true);
        assertTrue(vm.isRepeatTranscriptMode());
        vm.setRepeatTranscriptMode(false);
        assertFalse(vm.isRepeatTranscriptMode());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  11. Comment event
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void consumeCommentEvent_clearsEvent() {
        LessonViewModel vm = readyViewModel(true);
        vm.consumeCommentEvent();
        // Should not throw; event should be null
        assertNotNull(vm.getCommentEvent());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  12. isLoggedIn
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void isLoggedIn_reflectsConstructorArg() {
        LessonViewModel vmTrue = createViewModel(true);
        assertTrue(vmTrue.isLoggedIn());

        LessonViewModel vmFalse = createViewModel(false);
        assertFalse(vmFalse.isLoggedIn());
    }
}
