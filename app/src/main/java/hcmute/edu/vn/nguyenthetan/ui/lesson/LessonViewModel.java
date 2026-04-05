package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.CheckDictationAnswerUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.comment.GetCommentsUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonProgressUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.GetLessonSessionUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSentenceStatusUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.lesson.SaveSpeakingAttemptUseCase;

public class LessonViewModel extends ViewModel {

    public static class UiState {
        public int selectedTab;
        public String title;
        public String subtitle;
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
        public int playbackPercent;
        public boolean playing;
        public String speakingReference;
        public boolean recording;
        public String recordingLabel;
        public String bestScore;
        public String bestTranscript;
        public String currentScore;
        public String currentTranscript;
        public boolean speakingNextEnabled;
        public List<Comment> comments;
    }

    private final GetLessonSessionUseCase getLessonSessionUseCase;
    private final GetLessonProgressUseCase getLessonProgressUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final CheckDictationAnswerUseCase checkDictationAnswerUseCase;
    private final SaveSentenceStatusUseCase saveSentenceStatusUseCase;
    private final SaveSpeakingAttemptUseCase saveSpeakingAttemptUseCase;
    private final long lessonId;
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final Map<Long, SentenceStatus> sentenceStatuses = new LinkedHashMap<>();

    private LessonSession lessonSession;
    private List<Comment> comments = new ArrayList<>();
    private int selectedTab;
    private int currentIndex;
    private String inputText = "";
    private DictationFeedback feedback;
    private SpeakingAttempt currentAttempt;
    private boolean recording;
    private boolean playing;
    private long playbackPosition;
    private CountDownTimer timer;
    private boolean loaded;

    public LessonViewModel(
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

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        lessonSession = getLessonSessionUseCase.execute(lessonId);
        LessonProgress lessonProgress = getLessonProgressUseCase.execute(lessonId);
        sentenceStatuses.clear();
        sentenceStatuses.putAll(lessonProgress.getSentenceStatuses());
        currentIndex = lessonProgress.getCurrentSentenceIndex();
        currentAttempt = lessonProgress.getCurrentAttempt();
        if (sentenceStatuses.get(getCurrentSentence().getId()) == SentenceStatus.NOT_STARTED) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        comments = getCommentsUseCase.execute(getCurrentSentence().getId());
        loaded = true;
        publish();
    }

    public void selectTab(int tabIndex) {
        selectedTab = tabIndex;
        publish();
    }

    public void onInputChanged(String value) {
        inputText = value == null ? "" : value;
    }

    public void togglePlayback() {
        if (lessonSession == null) {
            return;
        }
        if (playing) {
            stopTimer();
            publish();
            return;
        }
        Sentence sentence = getCurrentSentence();
        final long duration = sentence.getDurationMillis();
        long remaining = Math.max(duration - playbackPosition, 0);
        if (remaining == 0) {
            playbackPosition = 0;
            remaining = duration;
        }
        playing = true;
        timer = new CountDownTimer(remaining, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                playbackPosition = duration - millisUntilFinished;
                publish();
            }

            @Override
            public void onFinish() {
                playing = false;
                playbackPosition = duration;
                publish();
            }
        };
        timer.start();
        publish();
    }

    public void replay() {
        playbackPosition = 0;
        stopTimer();
        togglePlayback();
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

    public void selectSentence(int index) {
        if (lessonSession == null || index < 0 || index >= lessonSession.getSentences().size()) {
            return;
        }
        stopTimer();
        currentIndex = index;
        feedback = null;
        inputText = "";
        playbackPosition = 0;
        if (sentenceStatuses.get(getCurrentSentence().getId()) == SentenceStatus.NOT_STARTED) {
            sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            saveSentenceStatusUseCase.execute(lessonId, getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
        }
        comments = getCommentsUseCase.execute(getCurrentSentence().getId());
        publish();
    }

    public void toggleRecording() {
        recording = !recording;
        if (!recording) {
            int score = (currentIndex % 2 == 0) ? 78 : 65;
            currentAttempt = new SpeakingAttempt(score, getCurrentSentence().getContent(), score >= lessonSession.getPassThreshold()
                    ? "Clear enough to pass. Keep that rhythm."
                    : "You are close. Focus on consonant endings.");
            saveSpeakingAttemptUseCase.execute(lessonId, getCurrentSentence().getId(), currentAttempt, lessonSession.getPassThreshold());
            if (score >= lessonSession.getPassThreshold()) {
                sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.COMPLETED);
            } else {
                sentenceStatuses.put(getCurrentSentence().getId(), SentenceStatus.IN_PROGRESS);
            }
        }
        publish();
    }

    public void retrySpeaking() {
        recording = false;
        currentAttempt = new SpeakingAttempt(58, "Practice and tap record again.", "Try once more with clearer pauses.");
        publish();
    }

    public void playTranscriptRow(int position) {
        selectSentence(position);
        replay();
    }

    private Sentence getCurrentSentence() {
        return lessonSession.getSentences().get(currentIndex);
    }

    private void publish() {
        if (lessonSession == null) {
            return;
        }
        Sentence current = getCurrentSentence();
        UiState state = new UiState();
        state.selectedTab = selectedTab;
        state.title = lessonSession.getTitle();
        state.subtitle = lessonSession.getCategoryTitle() + " - " + lessonSession.getLevel();
        state.sentenceCounter = String.format(Locale.US, "%d/%d", currentIndex + 1, lessonSession.getSentences().size());
        state.currentStatus = sentenceStatuses.get(current.getId());
        state.progressPercent = calculateProgressPercent();
        state.hint = current.getHintText().isEmpty() ? "Hint: no proper noun hint for this sentence." : "Hint: " + current.getHintText();
        state.inputText = inputText;
        state.showFeedback = feedback != null;
        state.feedbackTitle = feedback != null ? feedback.getTitle() : "";
        state.correctWords = feedback != null ? feedback.getCorrectWords() : "";
        state.newHint = feedback != null ? feedback.getNewHint() : "";
        state.maskedWords = feedback != null ? feedback.getMaskedWords() : "";
        state.showNextButton = feedback != null && currentIndex < lessonSession.getSentences().size() - 1;
        state.transcriptSentence = current.getContent();
        state.transcriptRows = buildTranscriptRows();
        state.playbackTime = formatTime(playbackPosition) + " / " + formatTime(current.getDurationMillis());
        state.playbackPercent = current.getDurationMillis() == 0 ? 0 : (int) ((playbackPosition * 100f) / current.getDurationMillis());
        state.playing = playing;
        state.speakingReference = current.getContent();
        state.recording = recording;
        state.recordingLabel = recording ? "Recording... tap again to stop" : "Tap to record";
        state.bestScore = "Best Score: " + lessonSession.getBestAttempt().getScore() + "/100";
        state.bestTranscript = lessonSession.getBestAttempt().getTranscript() + "\n" + lessonSession.getBestAttempt().getFeedback();
        state.currentScore = "Current Attempt: " + currentAttempt.getScore() + "/100";
        state.currentTranscript = currentAttempt.getTranscript() + "\n" + currentAttempt.getFeedback();
        state.speakingNextEnabled = currentAttempt.getScore() >= lessonSession.getPassThreshold();
        state.comments = comments;
        uiState.setValue(state);
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

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        playing = false;
    }

    @Override
    protected void onCleared() {
        stopTimer();
        super.onCleared();
    }
}
