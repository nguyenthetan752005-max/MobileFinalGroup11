package hcmute.edu.vn.nguyenthetan.ui.lesson;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.SyncSentenceCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;

public class LessonViewModel extends ViewModel {

    public static final int TAB_DICTATION = 0;
    public static final int TAB_TRANSCRIPT = 1;
    public static final int TAB_SPEAKING = 2;

    public static class UiState {
        public int selectedTab;
        public String title;
        public String subtitle;
        public String mediaSummary;
        public String sentenceCounter;
        public SentenceStatus currentStatus;
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
        public List<TranscriptAdapter.Row> transcriptRows;
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
        public boolean showSpeakingActions;
        public boolean speakingNextEnabled;
        public List<Comment> comments;
    }

    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final SyncSentenceCommentsUseCase syncSentenceCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final long lessonId;
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final Map<Long, SentenceStatus> sentenceStatuses = new LinkedHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
    private boolean loaded;

    public LessonViewModel(
            GetLessonSessionUseCase getLessonSessionUseCase,
            GetLessonProgressUseCase getLessonProgressUseCase,
            GetCommentsUseCase getCommentsUseCase,
            SyncSentenceCommentsUseCase syncSentenceCommentsUseCase,
            CheckDictationAnswerUseCase checkDictationAnswerUseCase,
            SaveSentenceStatusUseCase saveSentenceStatusUseCase,
            SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase,
            long lessonId
    ) {
        this.getLessonSessionUseCase = getLessonSessionUseCase;
        this.getLessonProgressUseCase = getLessonProgressUseCase;
        this.getCommentsUseCase = getCommentsUseCase;
        this.syncSentenceCommentsUseCase = syncSentenceCommentsUseCase;
        this.checkDictationAnswerUseCase = checkDictationAnswerUseCase;
        this.saveSentenceStatusUseCase = saveSentenceStatusUseCase;
        this.saveSpeakingAttemptUseCase = saveSpeakingAttemptUseCase;
        this.lessonId = lessonId;
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        loadingState.setValue(true);
        executorService.execute(() -> {
            lessonSession = getLessonSessionUseCase.execute(lessonId);
            LessonProgress lessonProgress = getLessonProgressUseCase.execute(lessonId);
            sentenceStatuses.clear();
            sentenceStatuses.putAll(lessonProgress.getSentenceStatuses());
            currentIndex = lessonProgress.getCurrentSentenceIndex();
            currentAttempt = lessonProgress.getCurrentAttempt();
            bestAttempt = lessonSession.getBestAttempt();
            if (lessonSession == null || lessonSession.getSentences().isEmpty()) {
                loaded = true;
                uiState.postValue(new UiState());
                loadingState.postValue(false);
                return;
            }
            if (sentenceStatuses.get(getCurrentSentence().getId()) == SentenceStatus.NOT_STARTED) {
                sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
                saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            }
            comments = getCommentsUseCase.execute(getCurrentSentence().getId());
            loaded = true;
            publish();
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

    public void completePlayback(long durationMillis) {
        playbackDuration = Math.max(durationMillis, playbackDuration);
        playbackPosition = 0L;
        playing = false;
        publish();
    }

    public void checkAnswer() {
        if (getCurrentSentence() == null || inputText.trim().isEmpty()) {
            return;
        }
        feedback = checkDictationAnswerUseCase.execute(getCurrentSentence(), inputText);
        if (feedback.isCorrect()) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.COMPLETED);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.COMPLETED);
            inputText = feedback.getFullAnswer();
        } else {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        publish();
    }

    public void skip() {
        sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.SKIPPED);
        saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.SKIPPED);
        feedback = new DictationFeedback(false, "Skipped. Review the answer before moving on.", getCurrentSentence().getContent(), "", "", "");
        publish();
    }

    public void nextSentence() {
        selectSentence(currentIndex + 1);
    }

    public void previousSentence() {
        selectSentence(currentIndex - 1);
    }

    public void refreshComments() {
        if (getCurrentSentence() != null) {
            long currentSentenceId = getCurrentSentence().getId();
            syncSentenceCommentsUseCase.execute(currentSentenceId, () -> {
                if (getCurrentSentence() != null && getCurrentSentence().getId() == currentSentenceId) {
                    comments = getCommentsUseCase.execute(currentSentenceId);
                    publish();
                }
            }, null);
        }
    }

    public void selectSentence(int index) {
        if (lessonSession == null || index < 0 || index >= lessonSession.getSentences().size()) {
            return;
        }
        resetPlaybackInternal();
        currentIndex = index;
        feedback = null;
        inputText = "";
        currentAttempt = null;
        bestAttempt = null;
        if (sentenceStatuses.get(getCurrentSentence().getId()) == SentenceStatus.NOT_STARTED) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        comments = getCommentsUseCase.execute(getCurrentSentence().getId());
        publish();
    }


    public void applyDictationFeedback(DictationFeedback result, boolean markCompleted, boolean markSkipped) {
        feedback = result;
        if (result != null && result.isCorrect() && markCompleted) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.COMPLETED);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.COMPLETED);
            inputText = result.getFullAnswer();
        } else if (markSkipped) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.SKIPPED);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.SKIPPED);
        } else {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        publish();
    }

    public long getCurrentSentenceId() {
        return lessonSession == null || lessonSession.getSentences().isEmpty() ? 0L : getCurrentSentence().getId();
    }

    public String getCurrentSentenceContent() {
        return lessonSession == null || lessonSession.getSentences().isEmpty() ? "" : getCurrentSentence().getContent();
    }

    public String getCurrentHintText() {
        return lessonSession == null || lessonSession.getSentences().isEmpty() ? "" : getCurrentSentence().getHintText();
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
        saveSpeakingAttemptUseCase.execute(lessonId, getCurrentSentence().getId(), currentAttempt, lessonSession.getPassThreshold());
        if (score >= lessonSession.getPassThreshold()) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.COMPLETED);
        } else {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
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

    private Sentence getCurrentSentence() {
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
        state.progressPercent = calculateProgressPercent();
        state.hint = resolveHintText(current);
        state.inputText = inputText;
        state.showFeedback = feedback != null;
        state.feedbackTitle = feedback != null ? feedback.getTitle() : "";
        state.correctWords = feedback != null ? feedback.getCorrectWords() : "";
        state.newHint = feedback != null ? feedback.getNewHint() : "";
        state.maskedWords = feedback != null ? feedback.getMaskedWords() : "";
        state.showNextButton = feedback != null && currentIndex < lessonSession.getSentences().size() - 1;
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
        state.showSpeakingActions = currentAttempt != null;
        state.speakingNextEnabled = !speakingBusy && currentAttempt != null && currentAttempt.getScore() >= lessonSession.getPassThreshold();
        state.comments = comments;
        uiState.postValue(state);
    }

    private List<TranscriptAdapter.Row> buildTranscriptRows() {
        List<TranscriptAdapter.Row> rows = new ArrayList<>();
        for (int i = 0; i < lessonSession.getSentences().size(); i++) {
            Sentence sentence = lessonSession.getSentences().get(i);
            rows.add(new TranscriptAdapter.Row(i + 1, sentence.getContent(), sentenceStatuses.get(sentence.getId()), i == currentIndex));
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
        if (currentSentence.getAudioUrl() != null && !currentSentence.getAudioUrl().trim().isEmpty()) {
            return "Audio sentence ready";
        }
        return "No media source for this sentence";
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
            hint.append("Proper Nouns: ").append(android.text.TextUtils.join(", ", formatted));
        }
        if (hint.length() == 0) {
            hint.append("No hints available for this sentence.");
        }
        return hint.toString();
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
