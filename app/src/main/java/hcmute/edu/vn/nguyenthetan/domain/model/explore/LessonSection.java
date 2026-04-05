package hcmute.edu.vn.nguyenthetan.domain.model.explore;

import java.util.Collections;
import java.util.List;

public class LessonSection {

    private final long id;
    private final String title;
    private final List<LessonSummary> lessons;

    public LessonSection(long id, String title, List<LessonSummary> lessons) {
        this.id = id;
        this.title = title;
        this.lessons = Collections.unmodifiableList(lessons);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<LessonSummary> getLessons() {
        return lessons;
    }
}
