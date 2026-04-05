package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

import java.util.Collections;
import java.util.List;

public class LessonSession {

    private final long lessonId;
    private final String title;
    private final String categoryTitle;
    private final String level;
    private final int passThreshold;
    private final List<Sentence> sentences;
    private final SpeakingAttempt bestAttempt;

    public LessonSession(
            long lessonId,
            String title,
            String categoryTitle,
            String level,
            int passThreshold,
            List<Sentence> sentences,
            SpeakingAttempt bestAttempt
    ) {
        this.lessonId = lessonId;
        this.title = title;
        this.categoryTitle = categoryTitle;
        this.level = level;
        this.passThreshold = passThreshold;
        this.sentences = Collections.unmodifiableList(sentences);
        this.bestAttempt = bestAttempt;
    }

    public long getLessonId() {
        return lessonId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public String getLevel() {
        return level;
    }

    public int getPassThreshold() {
        return passThreshold;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public SpeakingAttempt getBestAttempt() {
        return bestAttempt;
    }
}
