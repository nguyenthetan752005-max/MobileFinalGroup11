package hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard;

import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;
import hcmute.edu.vn.nguyenthetan.domain.repository.LeaderboardRepository;

public class GetLeaderboardUseCase {

    private final LeaderboardRepository repository;

    public GetLeaderboardUseCase(LeaderboardRepository repository) {
        this.repository = repository;
    }

    public LeaderboardData execute() {
        return repository.getLeaderboard();
    }
}
