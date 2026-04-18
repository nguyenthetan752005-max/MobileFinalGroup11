package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LessonProgress {

    private final Map<Long, SentenceStatus> sentenceStatuses;
    private final int currentSentenceIndex;
    private final SpeakingAttempt currentAttempt;

    public LessonProgress(
            Map<Long, SentenceStatus> sentenceStatuses,
            int currentSentenceIndex,
            SpeakingAttempt currentAttempt
    ) {
        this.sentenceStatuses = Collections.unmodifiableMap(new LinkedHashMap<>(sentenceStatuses));
        this.currentSentenceIndex = currentSentenceIndex;
        this.currentAttempt = currentAttempt;
    }

    public Map<Long, SentenceStatus> getSentenceStatuses() {
        return sentenceStatuses;
    }

    public int getCurrentSentenceIndex() {
        return currentSentenceIndex;
    }

    public SpeakingAttempt getCurrentAttempt() {
        return currentAttempt;
    }
}
