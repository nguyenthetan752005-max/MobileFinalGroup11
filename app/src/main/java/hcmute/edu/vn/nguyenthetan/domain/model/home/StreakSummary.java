package hcmute.edu.vn.nguyenthetan.domain.model.home;

import java.util.Collections;
import java.util.List;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.MoodState;


public class StreakSummary {

    private final int currentDays;
    private final int missedDays;
    private final boolean broken;
    private final String activeMood;
    private final String quote;
    private final List<Boolean> weekStatus;
    private final List<MoodState> moods;

    public StreakSummary(
            int currentDays,
            int missedDays,
            boolean broken,
            String activeMood,
            String quote,
            List<Boolean> weekStatus,
            List<MoodState> moods
    ) {
        this.currentDays = currentDays;
        this.missedDays = missedDays;
        this.broken = broken;
        this.activeMood = activeMood;
        this.quote = quote;
        this.weekStatus = Collections.unmodifiableList(weekStatus);
        this.moods = Collections.unmodifiableList(moods);
    }

    public int getCurrentDays() {
        return currentDays;
    }

    public int getMissedDays() {
        return missedDays;
    }

    public boolean isBroken() {
        return broken;
    }

    public String getActiveMood() {
        return activeMood;
    }

    public String getQuote() {
        return quote;
    }

    public List<Boolean> getWeekStatus() {
        return weekStatus;
    }

    public List<MoodState> getMoods() {
        return moods;
    }
}
