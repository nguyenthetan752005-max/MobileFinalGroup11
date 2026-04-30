package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.SpeakingAudioUrls;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationResult;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingEvaluation;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
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
import hcmute.edu.vn.nguyenthetan.ui.lesson.mapper.DictationFeedbackMapper;

public class LessonViewModel extends ViewModel {

    public static final int TAB_DICTATION = 0;
    public static final int TAB_TRANSCRIPT = 1;
    public static final int TAB_SPEAKING = 2;

    public enum CommentOpResult {
        ADD_SUCCESS,
        ADD_FAIL,
        DELETE_SUCCESS,
        DELETE_FAIL
    }

    public static class UiState {
        public int selectedTab;
        public String title;
        public String subtitle;
        public String mediaSummary;
        public String sentenceCounter;
        public SentenceStatus currentStatus;
        public boolean hasPreviousSentence;
        public boolean hasNextSentence;
        public int progressPercent;
        public String hint;
        public String inputText;
        public boolean showFeedback;
        public String feedbackTitle;
        public String correctWords;
        public String newHint;
        public String maskedWords;
        public boolean showNextButton;
        public String transcriptSentence;
        public List<LessonTranscriptRow> transcriptRows;
        public String playbackTime;
        public String playbackLabel;
        public int playbackPercent;
        public boolean playing;
        public boolean canPlayAudio;
        public boolean videoLesson;
        public boolean showDictationTab;
        public boolean showTranscriptTab;
        public boolean showSpeakingTab;
        public String currentAudioUrl;
        public String youtubeVideoId;
        public Double startTime;
        public Double endTime;
        public long currentSentenceId;
        public String speakingReference;
        public boolean recording;
        public boolean speakingBusy;
        public String recordingLabel;
        public String bestScore;
        public String bestTranscript;
        public String bestAudioUrl;
        public String currentScore;
        public String currentTranscript;
        public String currentUserAudioUrl;
        public boolean showSpeakingSkipButton;
        public boolean showSpeakingNextButton;
        public boolean showSpeakingActions;
        public boolean speakingNextEnabled;
        public int commentCount;
        public List<Comment> comments;
    }

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
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<CommentOpResult> commentEvent = new MutableLiveData<>();
    private final Map<Long, SentenceStatus> sentenceStatuses = new LinkedHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LessonSession lessonSession;
    private List<Comment> comments = new ArrayList<>();
    private int selectedTab;
    private int currentIndex;
    private String inputText = "";
    private DictationFeedback feedback;
    private SpeakingAttempt currentAttempt;
    private SpeakingAttempt bestAttempt;
    private boolean recording;
    private boolean playing;
    private long playbackPosition;
    private long playbackDuration;
    private boolean speakingBusy;
    private String speakingBusyLabel = "";
    private boolean repeatTranscriptMode;
    private boolean autoScrollTranscriptMode = true;
    private boolean loaded;

    public LessonViewModel(
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

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<CommentOpResult> getCommentEvent() {
        return commentEvent;
    }

    public void consumeCommentEvent() {
        commentEvent.setValue(null);
    }

    public void load() {
        if (loaded) {
            return;
        }
        loadingState.setValue(true);
        executorService.execute(() -> {
            lessonSession = getLessonSessionUseCase.execute(lessonId);
            if (lessonSession == null || lessonSession.getSentences().isEmpty()) {
                loaded = true;
                uiState.postValue(new UiState());
                loadingState.postValue(false);
                return;
            }
            LessonProgress lessonProgress = getLessonProgressUseCase.execute(lessonId);
            sentenceStatuses.clear();
            sentenceStatuses.putAll(lessonProgress.getSentenceStatuses());
            currentIndex = lessonProgress.getCurrentSentenceIndex();
            currentAttempt = lessonProgress.getCurrentAttempt();
            bestAttempt = lessonSession.getBestAttempt();
            SentenceStatus currentStatus = sentenceStatuses.get(getCurrentSentence().getId());
            if (currentStatus != SentenceStatus.COMPLETED && currentStatus != SentenceStatus.SKIPPED) {
                updateSentenceStatus(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            }
            comments = getCommentsUseCase.execute(getCurrentSentence().getId());
            loaded = true;
            publish();
            syncCommentsForSentence(getCurrentSentence().getId());
            loadingState.postValue(false);
        });
    }

    public void loadWithTargetSentence(long targetSentenceId) {
        if (loaded) {
            return;
        }
        loadingState.setValue(true);
        executorService.execute(() -> {
            lessonSession = getLessonSessionUseCase.execute(lessonId);
            if (lessonSession == null || lessonSession.getSentences().isEmpty()) {
                loaded = true;
                uiState.postValue(new UiState());
                loadingState.postValue(false);
                return;
            }
            LessonProgress lessonProgress = getLessonProgressUseCase.execute(lessonId);
            sentenceStatuses.clear();
            sentenceStatuses.putAll(lessonProgress.getSentenceStatuses());
            int targetIndex = -1;
            for (int i = 0; i < lessonSession.getSentences().size(); i++) {
                if (lessonSession.getSentences().get(i).getId() == targetSentenceId) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex >= 0) {
                currentIndex = targetIndex;
            } else {
                currentIndex = lessonProgress.getCurrentSentenceIndex();
            }
            currentAttempt = lessonProgress.getCurrentAttempt();
            bestAttempt = lessonSession.getBestAttempt();
            SentenceStatus currentStatus = sentenceStatuses.get(getCurrentSentence().getId());
            if (currentStatus != SentenceStatus.COMPLETED && currentStatus != SentenceStatus.SKIPPED) {
                updateSentenceStatus(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            }
            comments = getCommentsUseCase.execute(getCurrentSentence().getId());
            loaded = true;
            publish();
            syncCommentsForSentence(getCurrentSentence().getId());
            loadingState.postValue(false);
        });
    }

    public void selectTab(int tabIndex) {
        selectedTab = tabIndex;
        publish();
    }

    public void onInputChanged(String value) {
        inputText = value == null ? "" : value;
    }

    public void togglePlayback() {
        playing = !playing;
        if (playing && getCurrentSentence() != null && playbackPosition >= resolvePlaybackDuration(getCurrentSentence())) {
            playbackPosition = 0L;
        }
        publish();
    }

    public void replay() {
        playbackPosition = 0L;
        playbackDuration = 0L;
        playing = true;
        publish();
    }

    public void pausePlayback() {
        playing = false;
        publish();
    }

    public void resetPlayback() {
        playing = false;
        playbackPosition = 0L;
        playbackDuration = 0L;
        publish();
    }

    public void updatePlaybackProgress(long positionMillis, long durationMillis, boolean isPlaying) {
        playbackPosition = Math.max(positionMillis, 0L);
        if (durationMillis > 0L) {
            playbackDuration = durationMillis;
        }
        playing = isPlaying;
        publish();
    }

    public void syncVideoSentence(long positionMillis) {
        if (lessonSession == null || lessonSession.getSentences().isEmpty()) return;
        float currentSecond = positionMillis / 1000f;
        int newIndex = -1;
        for (int i = 0; i < lessonSession.getSentences().size(); i++) {
            Sentence s = lessonSession.getSentences().get(i);
            float start = s.getStartTime() != null ? s.getStartTime().floatValue() : 0f;
            float end = s.getEndTime() != null ? s.getEndTime().floatValue() : Float.MAX_VALUE;
            if (currentSecond >= start && currentSecond < end) {
                newIndex = i;
                break;
            }
        }
        if (newIndex >= 0 && newIndex != currentIndex) {
            selectSentence(newIndex, true);
        }
    }

    public void completePlayback(long durationMillis) {
        playbackDuration = Math.max(durationMillis, playbackDuration);
        playbackPosition = 0L;
        playing = false;
        publish();
    }

    public void setRepeatTranscriptMode(boolean enabled) {
        if (repeatTranscriptMode == enabled) return;
        repeatTranscriptMode = enabled;
        publish();
    }

    public boolean isRepeatTranscriptMode() {
        return repeatTranscriptMode;
    }

    public boolean isAutoScrollTranscriptMode() {
        return autoScrollTranscriptMode;
    }

    public void setAutoScrollTranscriptMode(boolean enabled) {
        this.autoScrollTranscriptMode = enabled;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public void checkAnswer() {
        Sentence current = getCurrentSentence();
        if (current == null || inputText.trim().isEmpty()) {
            return;
        }
        feedback = checkDictationAnswerUseCase.execute(current, inputText);
        if (feedback.isCorrect()) {
            updateSentenceStatus(current.getId(), SentenceStatus.COMPLETED);
            inputText = feedback.getFullAnswer();
        } else {
            updateSentenceStatus(current.getId(), SentenceStatus.IN_PROGRESS);
        }
        publish();
    }

    public void skip() {
        Sentence current = getCurrentSentence();
        if (current == null) return;
        updateSentenceStatus(current.getId(), SentenceStatus.SKIPPED);
        feedback = new DictationFeedback(false, "Skipped. Review the answer before moving on.", current.getContent(), "", "", "");
        publish();
    }

    public void skipSpeakingSentence() {
        Sentence current = getCurrentSentence();
        if (current == null) return;
        speakingBusy = false;
        speakingBusyLabel = "";
        recording = false;
        updateSentenceStatus(current.getId(), SentenceStatus.SKIPPED);
        publish();
    }

    public void nextSentence() {
        selectSentence(currentIndex + 1);
    }

    public void previousSentence() {
        selectSentence(currentIndex - 1);
    }

    public void refreshComments() {
        Sentence current = getCurrentSentence();
        if (current != null) {
            syncCommentsForSentence(current.getId());
        }
    }

    public void selectSentence(int index) {
        selectSentence(index, false);
    }

    public void selectSentence(int index, boolean seamless) {
        if (lessonSession == null || index < 0 || index >= lessonSession.getSentences().size()) {
            return;
        }
        if (!seamless) {
            resetPlaybackInternal();
        }
        currentIndex = index;
        feedback = null;
        inputText = "";
        currentAttempt = null;
        bestAttempt = null;
        Sentence current = getCurrentSentence();
        if (current == null) return;
        SentenceStatus existingStatus = sentenceStatuses.get(current.getId());
        if (existingStatus != SentenceStatus.COMPLETED && existingStatus != SentenceStatus.SKIPPED) {
            updateSentenceStatus(current.getId(), SentenceStatus.IN_PROGRESS);
        }
        publish(); // Publish immediately so UI state reflects new sentence

        // Room DB access must run off main thread
        executorService.execute(() -> {
            comments = getCommentsUseCase.execute(current.getId());
            mainHandler.post(this::publish);
            syncCommentsForSentence(current.getId());
        });
    }

    public void applyDictationFeedback(DictationFeedback result, boolean markCompleted, boolean markSkipped) {
        Sentence current = getCurrentSentence();
        if (current == null) return;
        feedback = result;
        if (result != null && result.isCorrect() && markCompleted) {
            updateSentenceStatus(current.getId(), SentenceStatus.COMPLETED);
            inputText = result.getFullAnswer();
        } else if (markSkipped) {
            updateSentenceStatus(current.getId(), SentenceStatus.SKIPPED);
        } else {
            updateSentenceStatus(current.getId(), SentenceStatus.IN_PROGRESS);
        }
        publish();
    }

    public long getCurrentSentenceId() {
        Sentence current = getCurrentSentence();
        return current == null ? 0L : current.getId();
    }

    public String getCurrentSentenceContent() {
        Sentence current = getCurrentSentence();
        return current == null ? "" : current.getContent();
    }

    public String getCurrentHintText() {
        Sentence current = getCurrentSentence();
        return current == null ? "" : current.getHintText();
    }

    public void setRecordingState(boolean active) {
        recording = active;
        if (active) {
            speakingBusy = false;
            speakingBusyLabel = "";
        }
        publish();
    }

    public void setSpeakingBusy(String label) {
        recording = false;
        speakingBusy = true;
        speakingBusyLabel = label == null ? "" : label;
        publish();
    }

    public void applySpeakingEvaluation(int score, String transcript, String feedback, String audioUrl, SpeakingAttempt bestAttemptFromServer) {
        speakingBusy = false;
        speakingBusyLabel = "";
        recording = false;
        currentAttempt = new SpeakingAttempt(score, transcript == null ? "" : transcript, feedback == null ? "" : feedback, audioUrl == null ? "" : audioUrl);
        if (bestAttemptFromServer != null) {
            bestAttempt = bestAttemptFromServer;
        } else if (bestAttempt == null || score > bestAttempt.getScore()) {
            bestAttempt = currentAttempt;
        }
        executorService.execute(() ->
                saveSpeakingAttemptUseCase.execute(lessonId, getCurrentSentence().getId(), currentAttempt, lessonSession.getPassThreshold())
        );
        if (score >= lessonSession.getPassThreshold()) {
            updateSentenceStatus(getCurrentSentence().getId(), SentenceStatus.COMPLETED);
        } else {
            updateSentenceStatus(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        publish();
    }

    public void showSpeakingMessage(String message) {
        speakingBusy = false;
        speakingBusyLabel = "";
        recording = false;
        currentAttempt = new SpeakingAttempt(
                currentAttempt == null ? 0 : currentAttempt.getScore(),
                currentAttempt == null ? "" : currentAttempt.getTranscript(),
                message == null ? "" : message,
                currentAttempt == null ? "" : currentAttempt.getAudioUrl()
        );
        publish();
    }

    public void retrySpeaking() {
        recording = false;
        currentAttempt = new SpeakingAttempt(58, "Practice and tap record again.", "Try once more with clearer pauses.", "");
        publish();
    }

    public void playTranscriptRow(int position) {
        selectSentence(position);
        replay();
    }

    /**
     * Populates speaking results from a server API response without persisting to local DB.
     * Used when navigating to a sentence that already has speaking history on the server.
     */
    public void applySpeakingResultsFromServer(int currentScore, String currentTranscript,
            String currentFeedback, String currentAudioUrl,
            int bestScore, String bestTranscript,
            String bestFeedback, String bestAudioUrl) {
        if (currentScore > 0 || (currentTranscript != null && !currentTranscript.isEmpty())) {
            currentAttempt = new SpeakingAttempt(
                    currentScore,
                    currentTranscript == null ? "" : currentTranscript,
                    currentFeedback == null ? "" : currentFeedback,
                    currentAudioUrl == null ? "" : currentAudioUrl
            );
        }
        if (bestScore > 0 || (bestTranscript != null && !bestTranscript.isEmpty())) {
            bestAttempt = new SpeakingAttempt(
                    bestScore,
                    bestTranscript == null ? "" : bestTranscript,
                    bestFeedback == null ? "" : bestFeedback,
                    bestAudioUrl == null ? "" : bestAudioUrl
            );
        }
        publish();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Network-driven actions (moved out of LessonActivity).
    //  Each public method posts state mutations back to the main thread so the
    //  ViewModel keeps a single-writer invariant for its private fields.
    // ────────────────────────────────────────────────────────────────────────

    public void submitDictationCheck() {
        if (lessonSession == null) return;
        final long requestedSentenceId = getCurrentSentenceId();
        final String content = getCurrentSentenceContent();
        final String hint = getCurrentHintText();
        final String input = inputText.trim();
        if (input.isEmpty()) return;
        executorService.execute(() -> {
            DictationResult result = checkDictationOnlineUseCase.execute(requestedSentenceId, input);
            mainHandler.post(() -> {
                if (getCurrentSentenceId() != requestedSentenceId) return;
                if (result == null) {
                    checkAnswer();
                    return;
                }
                DictationFeedback fb = DictationFeedbackMapper.map(result, content, hint);
                applyDictationFeedback(fb, result.correct, false);
            });
        });
    }

    public void submitDictationSkip() {
        if (lessonSession == null) return;
        final long requestedSentenceId = getCurrentSentenceId();
        final String content = getCurrentSentenceContent();
        final String hint = getCurrentHintText();
        final long userId = (session != null && session.isLoggedIn()) ? session.getUserId() : 0L;
        executorService.execute(() -> {
            DictationResult result = skipDictationUseCase.execute(userId, requestedSentenceId);
            mainHandler.post(() -> {
                if (getCurrentSentenceId() != requestedSentenceId) return;
                if (result == null) {
                    skip();
                    return;
                }
                DictationFeedback fb = DictationFeedbackMapper.map(result, content, hint);
                applyDictationFeedback(fb, false, true);
            });
        });
    }

    /**
     * Uploads a recorded audio file for evaluation. The file is deleted after
     * upload regardless of outcome. UI strings are passed by the Activity so
     * the VM stays free of resource lookups.
     */
    public void evaluateSpeaking(File audioFile, String reference, long sentenceId,
                                 String busyLabel, String failureMessage) {
        if (audioFile == null || !audioFile.exists() || lessonSession == null) {
            showSpeakingMessage(failureMessage);
            return;
        }
        if (session == null || !session.isLoggedIn()) {
            audioFile.delete();
            return;
        }
        setSpeakingBusy(busyLabel);
        final long requestedSentenceId = sentenceId;
        executorService.execute(() -> {
            SpeakingEvaluation body = evaluateSpeakingUseCase.execute(audioFile, reference, requestedSentenceId);
            audioFile.delete();
            mainHandler.post(() -> {
                if (getCurrentSentenceId() != requestedSentenceId) return;
                if (body == null) {
                    showSpeakingMessage(failureMessage);
                    return;
                }
                String transcript = body.recognizedText == null ? "" : body.recognizedText;
                String feedbackMsg = body.feedback == null ? "" : body.feedback;
                if (body.accuracy > 0d) {
                    feedbackMsg = feedbackMsg.isEmpty()
                            ? String.format(Locale.US, "Accuracy: %.1f%%", body.accuracy)
                            : feedbackMsg + "\nAccuracy: " + String.format(Locale.US, "%.1f%%", body.accuracy);
                }
                SpeakingAttempt bestFromServer = body.bestResult;
                applySpeakingEvaluation(body.score, transcript, feedbackMsg, body.audioUrl, bestFromServer);
            });
        });
    }

    public void fetchSpeakingResultsForCurrentSentence() {
        if (lessonSession == null || !loggedIn || !isSpeakingEnabled()) return;
        final long requestedSentenceId = getCurrentSentenceId();
        if (requestedSentenceId <= 0L) return;
        executorService.execute(() -> {
            SpeakingEvaluation body = getSpeakingResultsUseCase.execute(requestedSentenceId);
            if (body == null) return;
            mainHandler.post(() -> {
                if (getCurrentSentenceId() != requestedSentenceId) return;
                int bestScore = 0;
                String bestTranscript = null, bestFeedback = null, bestAudioUrl = null;
                if (body.bestResult != null) {
                    bestScore = body.bestResult.getScore();
                    bestTranscript = body.bestResult.getTranscript();
                    bestFeedback = body.bestResult.getFeedback();
                    bestAudioUrl = body.bestResult.getAudioUrl();
                }
                applySpeakingResultsFromServer(
                        body.score, body.recognizedText, body.feedback, body.audioUrl,
                        bestScore, bestTranscript, bestFeedback, bestAudioUrl
                );
            });
        });
    }

    public void trackSessionTime(long durationMillis) {
        if (session == null || !session.isLoggedIn()) return;
        long userId = session.getUserId();
        int seconds = (int) (durationMillis / 1000L);
        if (userId <= 0L || seconds <= 0) return;
        executorService.execute(() -> trackLessonTimeUseCase.execute(userId, seconds));
    }

    public void submitComment(@Nullable Long parentCommentId, String content) {
        if (session == null || !session.isLoggedIn() || content == null || content.trim().isEmpty()) return;
        final long sentenceId = getCurrentSentenceId();
        final long authorId = session.getUserId();
        executorService.execute(() -> {
            boolean ok = addCommentUseCase.execute(sentenceId, authorId, content.trim(), parentCommentId);
            commentEvent.postValue(ok ? CommentOpResult.ADD_SUCCESS : CommentOpResult.ADD_FAIL);
            if (ok) {
                mainHandler.post(this::refreshComments);
            }
        });
    }

    public void voteComment(long commentId, boolean isLike) {
        if (session == null || !session.isLoggedIn()) return;
        final long userId = session.getUserId();
        executorService.execute(() -> {
            boolean ok = voteCommentUseCase.execute(commentId, userId, isLike);
            if (ok) {
                mainHandler.post(this::refreshComments);
            }
        });
    }

    public void deleteComment(long commentId) {
        if (session == null || !session.isLoggedIn()) return;
        final long userId = session.getUserId();
        executorService.execute(() -> {
            boolean ok = deleteCommentUseCase.execute(commentId, userId);
            commentEvent.postValue(ok ? CommentOpResult.DELETE_SUCCESS : CommentOpResult.DELETE_FAIL);
            if (ok) {
                mainHandler.post(this::refreshComments);
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────

    private void updateSentenceStatus(long sentenceId, SentenceStatus requestedStatus) {
        SentenceStatus currentStatus = sentenceStatuses.get(sentenceId);
        if (currentStatus == SentenceStatus.COMPLETED) {
            return;
        }
        sentenceStatuses.put(sentenceId, requestedStatus);
        
        if (!loggedIn) {
            return;
        }

        // Room DB access must not happen on the main thread
        executorService.execute(() ->
                saveSentenceStatusUseCase.execute(lessonId, sentenceId, requestedStatus)
        );
    }

    private Sentence getCurrentSentence() {
        if (lessonSession == null || lessonSession.getSentences().isEmpty()
                || currentIndex < 0 || currentIndex >= lessonSession.getSentences().size()) {
            return null;
        }
        return lessonSession.getSentences().get(currentIndex);
    }

    private void publish() {
        if (lessonSession == null) {
            return;
        }
        if (lessonSession.getSentences().isEmpty()) {
            uiState.postValue(new UiState());
            return;
        }
        Sentence current = getCurrentSentence();
        boolean videoLesson = isVideoLesson();
        boolean speakingEnabled = isSpeakingEnabled();
        boolean dictationEnabled = isDictationEnabled();
        selectedTab = normalizeSelectedTab(selectedTab, dictationEnabled, speakingEnabled);
        long effectiveDuration = resolvePlaybackDuration(current);

        UiState state = new UiState();
        state.selectedTab = selectedTab;
        state.title = lessonSession.getTitle();
        state.subtitle = lessonSession.getCategoryTitle() + " - " + lessonSession.getLevel();
        state.mediaSummary = buildMediaSummary(videoLesson, current);
        state.sentenceCounter = String.format(Locale.US, "%d/%d", currentIndex + 1, lessonSession.getSentences().size());
        state.currentStatus = sentenceStatuses.get(current.getId());
        state.hasPreviousSentence = currentIndex > 0;
        state.hasNextSentence = currentIndex < lessonSession.getSentences().size() - 1;
        state.progressPercent = calculateProgressPercent();
        state.hint = resolveHintText(current);
        state.inputText = inputText;
        state.showFeedback = feedback != null;
        state.feedbackTitle = feedback != null ? feedback.getTitle() : "";
        state.correctWords = feedback != null ? feedback.getCorrectWords() : "";
        state.newHint = feedback != null ? feedback.getNewHint() : "";
        state.maskedWords = feedback != null ? feedback.getMaskedWords() : "";
        boolean currentSkipped = state.currentStatus == SentenceStatus.SKIPPED;
        state.showNextButton = currentSkipped && state.hasNextSentence;
        state.transcriptSentence = current.getContent();
        state.transcriptRows = buildTranscriptRows();
        state.playbackTime = formatTime(playbackPosition) + " / " + formatTime(effectiveDuration);
        state.playbackLabel = videoLesson ? "Open video clip" : "Play sentence audio";
        state.playbackPercent = effectiveDuration == 0 ? 0 : (int) ((playbackPosition * 100f) / effectiveDuration);
        state.playing = playing;
        state.canPlayAudio = current.getAudioUrl() != null && !current.getAudioUrl().trim().isEmpty();
        state.videoLesson = videoLesson;
        state.showDictationTab = dictationEnabled;
        state.showTranscriptTab = true;
        state.showSpeakingTab = speakingEnabled;
        state.currentAudioUrl = current.getAudioUrl();
        state.youtubeVideoId = lessonSession.getYoutubeVideoId();
        state.startTime = current.getStartTime();
        state.endTime = current.getEndTime();
        state.currentSentenceId = current.getId();
        state.speakingReference = current.getContent();
        state.recording = recording;
        state.speakingBusy = speakingBusy;
        state.recordingLabel = speakingBusy
                ? speakingBusyLabel
                : (recording ? "Recording... tap again to stop" : "Tap the mic to start");
        state.bestScore = bestAttempt != null ? bestAttempt.getScore() + "/100" : "No score yet";
        state.bestTranscript = bestAttempt != null && !bestAttempt.getTranscript().isEmpty()
                ? bestAttempt.getTranscript() + "\n" + bestAttempt.getFeedback()
                : (bestAttempt != null && !bestAttempt.getFeedback().isEmpty()
                ? bestAttempt.getFeedback()
                : "Your strongest result will appear here.");
        String baseApiUrl = hcmute.edu.vn.nguyenthetan.BuildConfig.TUNGTUNG_API_BASE_URL;
        if (!baseApiUrl.endsWith("/")) {
            baseApiUrl += "/";
        }
        state.bestAudioUrl = bestAttempt != null && bestAttempt.getAudioUrl() != null && !bestAttempt.getAudioUrl().trim().isEmpty()
                ? baseApiUrl + "api/mobile/speaking/audio/best?sentenceId=" + current.getId()
                : null;
        state.currentScore = currentAttempt != null ? currentAttempt.getScore() + "/100" : "Waiting";
        state.currentTranscript = currentAttempt != null && !currentAttempt.getTranscript().isEmpty()
                ? currentAttempt.getTranscript() + "\n" + currentAttempt.getFeedback()
                : (currentAttempt != null && !currentAttempt.getFeedback().isEmpty()
                ? currentAttempt.getFeedback()
                : "Record once to get feedback.");
        state.currentUserAudioUrl = currentAttempt != null && currentAttempt.getAudioUrl() != null && !currentAttempt.getAudioUrl().trim().isEmpty()
                ? baseApiUrl + "api/mobile/speaking/audio/current?sentenceId=" + current.getId()
                : null;
        state.showSpeakingSkipButton = state.hasNextSentence && state.currentStatus != SentenceStatus.SKIPPED && state.currentStatus != SentenceStatus.COMPLETED;
        state.showSpeakingNextButton = state.hasNextSentence && (state.currentStatus == SentenceStatus.SKIPPED || state.currentStatus == SentenceStatus.COMPLETED);
        state.showSpeakingActions = currentAttempt != null;
        state.speakingNextEnabled = !speakingBusy && currentAttempt != null && currentAttempt.getScore() >= lessonSession.getPassThreshold();
        state.commentCount = countComments(comments);
        state.comments = comments;
        uiState.postValue(state);
    }

    private List<LessonTranscriptRow> buildTranscriptRows() {
        List<LessonTranscriptRow> rows = new ArrayList<>();
        for (int i = 0; i < lessonSession.getSentences().size(); i++) {
            Sentence sentence = lessonSession.getSentences().get(i);
            boolean isSelected = i == currentIndex;
            rows.add(new LessonTranscriptRow(i + 1, sentence.getContent(), sentenceStatuses.get(sentence.getId()), isSelected, isSelected && playing));
        }
        return rows;
    }

    private int calculateProgressPercent() {
        int completed = 0;
        for (SentenceStatus status : sentenceStatuses.values()) {
            if (status == SentenceStatus.COMPLETED) {
                completed++;
            }
        }
        return (int) ((completed * 100f) / Math.max(lessonSession.getSentences().size(), 1));
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    private long resolvePlaybackDuration(Sentence sentence) {
        if (playbackDuration > 0L) {
            return playbackDuration;
        }
        return sentence == null ? 0L : Math.max(sentence.getDurationMillis(), 0L);
    }

    private boolean isVideoLesson() {
        return lessonSession != null && "VIDEO".equalsIgnoreCase(lessonSession.getContentType());
    }

    private boolean isSpeakingEnabled() {
        if (lessonSession == null) {
            return false;
        }
        String practiceType = lessonSession.getCategoryPracticeType();
        return practiceType == null
                || practiceType.trim().isEmpty()
                || "SPEAKING".equalsIgnoreCase(practiceType)
                || "BOTH".equalsIgnoreCase(practiceType)
                || "LISTENING_SPEAKING".equalsIgnoreCase(practiceType);
    }

    private boolean isDictationEnabled() {
        if (lessonSession == null) {
            return false;
        }
        String practiceType = lessonSession.getCategoryPracticeType();
        return practiceType == null
                || practiceType.trim().isEmpty()
                || "LISTENING".equalsIgnoreCase(practiceType)
                || "BOTH".equalsIgnoreCase(practiceType)
                || "LISTENING_SPEAKING".equalsIgnoreCase(practiceType);
    }

    private int normalizeSelectedTab(int candidate, boolean dictationEnabled, boolean speakingEnabled) {
        if (candidate == TAB_DICTATION && dictationEnabled) {
            return TAB_DICTATION;
        }
        if (candidate == TAB_TRANSCRIPT) {
            return TAB_TRANSCRIPT;
        }
        if (candidate == TAB_SPEAKING && speakingEnabled) {
            return TAB_SPEAKING;
        }
        if (dictationEnabled) {
            return TAB_DICTATION;
        }
        if (speakingEnabled) {
            return TAB_SPEAKING;
        }
        return TAB_TRANSCRIPT;
    }

    private String buildMediaSummary(boolean videoLesson, Sentence currentSentence) {
        if (videoLesson) {
            long startSeconds = currentSentence.getStartTime() == null ? 0L : Math.round(currentSentence.getStartTime());
            long endSeconds = currentSentence.getEndTime() == null ? 0L : Math.round(currentSentence.getEndTime());
            if (endSeconds > startSeconds) {
                return "Video clip: " + formatTime(startSeconds * 1000L) + " - " + formatTime(endSeconds * 1000L);
            }
            return "Video clip from YouTube";
        }
        return "";
    }

    private String resolveHintText(Sentence current) {
        StringBuilder hint = new StringBuilder();
        if (current.getHintText() != null && !current.getHintText().isEmpty()) {
            hint.append("Hint: ").append(current.getHintText());
        }
        if (current.getProperNouns() != null && !current.getProperNouns().isEmpty()) {
            if (hint.length() > 0) hint.append("\n");

            // Format proper nouns, capitalizing them
            List<String> formatted = new ArrayList<>();
            for (String pn : current.getProperNouns()) {
                if (!pn.isEmpty()) {
                    formatted.add(pn.substring(0, 1).toUpperCase() + pn.substring(1));
                }
            }
            hint.append("Hint: ").append(android.text.TextUtils.join(", ", formatted));
        }
        return hint.toString();
    }

    private void syncCommentsForSentence(long sentenceId) {
        syncSentenceCommentsUseCase.execute(sentenceId, () -> {
            Sentence current = getCurrentSentence();
            if (current != null && current.getId() == sentenceId) {
                // Room DB access must run off main thread
                executorService.execute(() -> {
                    comments = getCommentsUseCase.execute(sentenceId);
                    mainHandler.post(this::publish);
                });
            }
        }, null);
    }

    private int countComments(List<Comment> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Comment comment : items) {
            total++;
            total += countComments(comment.getReplies());
        }
        return total;
    }

    private void resetPlaybackInternal() {
        playing = false;
        playbackPosition = 0L;
        playbackDuration = 0L;
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
