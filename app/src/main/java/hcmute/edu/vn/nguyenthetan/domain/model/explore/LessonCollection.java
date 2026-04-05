package hcmute.edu.vn.nguyenthetan.domain.model.explore;

import java.util.Collections;
import java.util.List;

public class LessonCollection {

    private final String categoryId;
    private final String title;
    private final String description;
    private final int totalLessons;
    private final List<LessonSection> sections;

    public LessonCollection(
            String categoryId,
            String title,
            String description,
            int totalLessons,
            List<LessonSection> sections
    ) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.totalLessons = totalLessons;
        this.sections = Collections.unmodifiableList(sections);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public List<LessonSection> getSections() {
        return sections;
    }
}
