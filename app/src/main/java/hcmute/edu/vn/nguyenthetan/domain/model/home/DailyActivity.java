package hcmute.edu.vn.nguyenthetan.domain.model.home;

public class DailyActivity {

    private final String dayLabel;
    private final int minutes;
    private final boolean highlighted;

    public DailyActivity(String dayLabel, int minutes, boolean highlighted) {
        this.dayLabel = dayLabel;
        this.minutes = minutes;
        this.highlighted = highlighted;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
