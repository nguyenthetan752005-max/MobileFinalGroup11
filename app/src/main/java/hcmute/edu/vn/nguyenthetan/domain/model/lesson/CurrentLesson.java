package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

public class CurrentLesson {

    private final long lessonId;
    private final String title;
    private final int completedSentences;
    private final int totalSentences;
    private final String primaryMode;
    private final String secondaryMode;

    public CurrentLesson(
            long lessonId,
            String title,
            int completedSentences,
            int totalSentences,
            String primaryMode,
            String secondaryMode
    ) {
        this.lessonId = lessonId;
        this.title = title;
        this.completedSentences = completedSentences;
        this.totalSentences = totalSentences;
        this.primaryMode = primaryMode;
        this.secondaryMode = secondaryMode;
    }

    public long getLessonId() {
        return lessonId;
    }

    public String getTitle() {
        return title;
    }

    public int getCompletedSentences() {
        return completedSentences;
    }

    public int getTotalSentences() {
        return totalSentences;
    }

    public String getPrimaryMode() {
        return primaryMode;
    }

    public String getSecondaryMode() {
        return secondaryMode;
    }
}
