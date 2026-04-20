package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;

public class LeaderboardUiState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR
    }

    private final Status status;
    private final LeaderboardData data;
    private final String message;

    private LeaderboardUiState(Status status, LeaderboardData data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static LeaderboardUiState idle() {
        return new LeaderboardUiState(Status.IDLE, null, null);
    }

    public static LeaderboardUiState loading() {
        return new LeaderboardUiState(Status.LOADING, null, null);
    }

    public static LeaderboardUiState success(LeaderboardData data) {
        return new LeaderboardUiState(Status.SUCCESS, data, null);
    }

    public static LeaderboardUiState empty() {
        return new LeaderboardUiState(Status.EMPTY, null, null);
    }

    public static LeaderboardUiState error(String message) {
        return new LeaderboardUiState(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public LeaderboardData getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
