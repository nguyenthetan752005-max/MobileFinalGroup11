package hcmute.edu.vn.nguyenthetan.domain.model.home;

public class StudyStats {

    private final String today;
    private final String thisWeek;
    private final int bestStreak;

    public StudyStats(String today, String thisWeek, int bestStreak) {
        this.today = today;
        this.thisWeek = thisWeek;
        this.bestStreak = bestStreak;
    }

    public String getToday() {
        return today;
    }

    public String getThisWeek() {
        return thisWeek;
    }

    public int getBestStreak() {
        return bestStreak;
    }
}
