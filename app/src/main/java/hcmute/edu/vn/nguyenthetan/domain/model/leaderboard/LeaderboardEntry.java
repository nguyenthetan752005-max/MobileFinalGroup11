package hcmute.edu.vn.nguyenthetan.domain.model.leaderboard;

public class LeaderboardEntry {

    private final int rank;
    private final String name;
    private final String avatarLabel;
    private final String activeTime;
    private final boolean currentUser;

    public LeaderboardEntry(int rank, String name, String avatarLabel, String activeTime, boolean currentUser) {
        this.rank = rank;
        this.name = name;
        this.avatarLabel = avatarLabel;
        this.activeTime = activeTime;
        this.currentUser = currentUser;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getAvatarLabel() {
        return avatarLabel;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public boolean isCurrentUser() {
        return currentUser;
    }
}
