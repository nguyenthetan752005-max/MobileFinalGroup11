package hcmute.edu.vn.nguyenthetan.domain.model.home;

public class Recommendation {

    private final String title;
    private final String level;
    private final int lessonCount;
    private final String practiceType;

    public Recommendation(String title, String level, int lessonCount, String practiceType) {
        this.title = title;
        this.level = level;
        this.lessonCount = lessonCount;
        this.practiceType = practiceType;
    }

    public String getTitle() {
        return title;
    }

    public String getLevel() {
        return level;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public String getPracticeType() {
        return practiceType;
    }
}
