package hcmute.edu.vn.nguyenthetan.domain.model.home;

import java.util.Collections;
import java.util.List;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.CurrentLesson;


public class HomeDashboard {

    private final String greeting;
    private final String subtitle;
    private final StreakSummary streakSummary;
    private final CurrentLesson currentLesson;
    private final StudyStats studyStats;
    private final List<Recommendation> recommendations;

    public HomeDashboard(
            String greeting,
            String subtitle,
            StreakSummary streakSummary,
            CurrentLesson currentLesson,
            StudyStats studyStats,
            List<Recommendation> recommendations
    ) {
        this.greeting = greeting;
        this.subtitle = subtitle;
        this.streakSummary = streakSummary;
        this.currentLesson = currentLesson;
        this.studyStats = studyStats;
        this.recommendations = Collections.unmodifiableList(recommendations);
    }

    public String getGreeting() {
        return greeting;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public StreakSummary getStreakSummary() {
        return streakSummary;
    }

    public CurrentLesson getCurrentLesson() {
        return currentLesson;
    }

    public StudyStats getStudyStats() {
        return studyStats;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }
}
