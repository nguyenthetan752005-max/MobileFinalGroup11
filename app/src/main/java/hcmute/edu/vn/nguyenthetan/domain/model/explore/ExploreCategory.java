package hcmute.edu.vn.nguyenthetan.domain.model.explore;

public class ExploreCategory {

    private final String id;
    private final String title;
    private final String levelRange;
    private final int lessonCount;
    private final String practiceType;
    private final String progressLabel;

    public ExploreCategory(
            String id,
            String title,
            String levelRange,
            int lessonCount,
            String practiceType,
            String progressLabel
    ) {
        this.id = id;
        this.title = title;
        this.levelRange = levelRange;
        this.lessonCount = lessonCount;
        this.practiceType = practiceType;
        this.progressLabel = progressLabel;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public String getPracticeType() {
        return practiceType;
    }

    public String getProgressLabel() {
        return progressLabel;
    }
}
