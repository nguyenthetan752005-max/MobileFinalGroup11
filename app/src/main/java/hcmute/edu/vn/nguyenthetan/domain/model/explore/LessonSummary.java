package hcmute.edu.vn.nguyenthetan.domain.model.explore;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;


public class LessonSummary {

    private final long lessonId;
    private final String title;
    private final String level;
    private final String practiceType;
    private final int completedSentences;
    private final int totalSentences;
    private final SentenceStatus status;

    public LessonSummary(
            long lessonId,
            String title,
            String level,
            String practiceType,
            int completedSentences,
            int totalSentences,
            SentenceStatus status
    ) {
        this.lessonId = lessonId;
        this.title = title;
        this.level = level;
        this.practiceType = practiceType;
        this.completedSentences = completedSentences;
        this.totalSentences = totalSentences;
        this.status = status;
    }

    public long getLessonId() {
        return lessonId;
    }

    public String getTitle() {
        return title;
    }

    public String getLevel() {
        return level;
    }

    public String getPracticeType() {
        return practiceType;
    }

    public int getCompletedSentences() {
        return completedSentences;
    }

    public int getTotalSentences() {
        return totalSentences;
    }

    public SentenceStatus getStatus() {
        return status;
    }
}
