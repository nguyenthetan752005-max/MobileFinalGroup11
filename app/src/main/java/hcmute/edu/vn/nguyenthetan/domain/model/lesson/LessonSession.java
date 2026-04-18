package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

import java.util.Collections;
import java.util.List;

public class LessonSession {

    private final long lessonId;
    private final String title;
    private final String categoryTitle;
    private final String categoryPracticeType;
    private final String level;
    private final String contentType;
    private final int passThreshold;
    private final String youtubeVideoId;
    private final List<Sentence> sentences;
    private final SpeakingAttempt bestAttempt;

    public LessonSession(
            long lessonId,
            String title,
            String categoryTitle,
            String categoryPracticeType,
            String level,
            String contentType,
            int passThreshold,
            String youtubeVideoId,
            List<Sentence> sentences,
            SpeakingAttempt bestAttempt
    ) {
        this.lessonId = lessonId;
        this.title = title;
        this.categoryTitle = categoryTitle;
        this.categoryPracticeType = categoryPracticeType;
        this.level = level;
        this.contentType = contentType;
        this.passThreshold = passThreshold;
        this.youtubeVideoId = youtubeVideoId;
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

    public String getCategoryPracticeType() {
        return categoryPracticeType;
    }

    public String getLevel() {
        return level;
    }

    public String getContentType() {
        return contentType;
    }

    public int getPassThreshold() {
        return passThreshold;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public SpeakingAttempt getBestAttempt() {
        return bestAttempt;
    }
}
