package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.dao.community.LeaderboardDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;
import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;
import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardEntry;
import hcmute.edu.vn.nguyenthetan.domain.repository.LeaderboardRepository;

public class RoomLeaderboardRepository implements LeaderboardRepository {

    private final LeaderboardDao leaderboardDao;

    public RoomLeaderboardRepository(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
    }

    @Override
    public LeaderboardData getLeaderboard() {
        LeaderboardMetaEntity meta = leaderboardDao.getMeta();
        int currentUserRank = meta == null ? 0 : meta.currentUserRank;
        String currentUserTime = meta == null || meta.currentUserTime == null ? "" : meta.currentUserTime;
        return new LeaderboardData(
                mapEntries(leaderboardDao.getByPeriod("WEEKLY")),
                mapEntries(leaderboardDao.getByPeriod("MONTHLY")),
                currentUserRank,
                currentUserTime
        );
    }

    private List<LeaderboardEntry> mapEntries(List<LeaderboardEntryEntity> entities) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (LeaderboardEntryEntity entity : entities) {
            entries.add(new LeaderboardEntry(
                    entity.rank,
                    entity.name,
                    entity.avatarLabel,
                    entity.activeTime,
                    entity.currentUser
            ));
        }
        return entries;
    }
}
