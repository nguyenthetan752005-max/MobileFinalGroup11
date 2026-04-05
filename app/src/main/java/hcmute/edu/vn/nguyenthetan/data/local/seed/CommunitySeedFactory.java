package hcmute.edu.vn.nguyenthetan.data.local.seed;

import java.util.Arrays;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;

final class CommunitySeedFactory {

    private CommunitySeedFactory() {
    }

    static List<CommentEntity> createComments() {
        return Arrays.asList(
                new CommentEntity(1L, 5003L, null, "Hieu Tran", "H", "2 hours ago", "This sentence is tricky because the speaker talks so fast.", 16, 1, 1),
                new CommentEntity(2L, 5003L, 1L, "Linh", "L", "30 min ago", "The hint saved me on the Michael Jackson sentence.", 4, 0, 2),
                new CommentEntity(3L, 5003L, null, "An Vo", "A", "5 hours ago", "I replayed it three times and finally got the rhythm right.", 9, 0, 3)
        );
    }

    static List<LeaderboardEntryEntity> createLeaderboardEntries() {
        return Arrays.asList(
                new LeaderboardEntryEntity("WEEKLY", 1, "Minh Triet", "MT", "5h 30m", false),
                new LeaderboardEntryEntity("WEEKLY", 2, "Duc Tam", "DT", "5h 10m", false),
                new LeaderboardEntryEntity("WEEKLY", 3, "Ngoc Anh", "NA", "4h 42m", false),
                new LeaderboardEntryEntity("WEEKLY", 4, "Hoang Vu", "HV", "4h 18m", false),
                new LeaderboardEntryEntity("WEEKLY", 5, "Bao Han", "BH", "4h 03m", false),
                new LeaderboardEntryEntity("WEEKLY", 6, "Thanh Lam", "TL", "3h 58m", false),
                new LeaderboardEntryEntity("WEEKLY", 7, "Tan Nguyen", "TN", "3h 44m", true),
                new LeaderboardEntryEntity("WEEKLY", 8, "Gia Han", "GH", "3h 26m", false),
                new LeaderboardEntryEntity("WEEKLY", 9, "Quoc Dat", "QD", "3h 08m", false),
                new LeaderboardEntryEntity("WEEKLY", 10, "Mai Phuong", "MP", "2h 57m", false),
                new LeaderboardEntryEntity("MONTHLY", 1, "Minh Triet", "MT", "18h 20m", false),
                new LeaderboardEntryEntity("MONTHLY", 2, "Duc Tam", "DT", "17h 40m", false),
                new LeaderboardEntryEntity("MONTHLY", 3, "Ngoc Anh", "NA", "16h 15m", false),
                new LeaderboardEntryEntity("MONTHLY", 4, "Hoang Vu", "HV", "14h 58m", false),
                new LeaderboardEntryEntity("MONTHLY", 5, "Bao Han", "BH", "14h 12m", false),
                new LeaderboardEntryEntity("MONTHLY", 6, "Thanh Lam", "TL", "13h 27m", false),
                new LeaderboardEntryEntity("MONTHLY", 7, "Tan Nguyen", "TN", "12h 44m", true),
                new LeaderboardEntryEntity("MONTHLY", 8, "Gia Han", "GH", "12h 06m", false),
                new LeaderboardEntryEntity("MONTHLY", 9, "Quoc Dat", "QD", "11h 53m", false),
                new LeaderboardEntryEntity("MONTHLY", 10, "Mai Phuong", "MP", "11h 11m", false)
        );
    }

    static LeaderboardMetaEntity createLeaderboardMeta() {
        return new LeaderboardMetaEntity(1L, 14, "3h 15m");
    }
}
