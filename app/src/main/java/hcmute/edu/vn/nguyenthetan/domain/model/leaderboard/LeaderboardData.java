package hcmute.edu.vn.nguyenthetan.domain.model.leaderboard;

import java.util.Collections;
import java.util.List;

public class LeaderboardData {

    private final List<LeaderboardEntry> weeklyEntries;
    private final List<LeaderboardEntry> monthlyEntries;
    private final int currentUserRank;
    private final String currentUserTime;

    public LeaderboardData(
            List<LeaderboardEntry> weeklyEntries,
            List<LeaderboardEntry> monthlyEntries,
            int currentUserRank,
            String currentUserTime
    ) {
        this.weeklyEntries = Collections.unmodifiableList(weeklyEntries);
        this.monthlyEntries = Collections.unmodifiableList(monthlyEntries);
        this.currentUserRank = currentUserRank;
        this.currentUserTime = currentUserTime;
    }

    public List<LeaderboardEntry> getWeeklyEntries() {
        return weeklyEntries;
    }

    public List<LeaderboardEntry> getMonthlyEntries() {
        return monthlyEntries;
    }

    public int getCurrentUserRank() {
        return currentUserRank;
    }

    public String getCurrentUserTime() {
        return currentUserTime;
    }
}
