package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.DailyActivityDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.ProfileDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.StreakDayDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.data.repository.support.RepositoryFormatters;
import hcmute.edu.vn.nguyenthetan.domain.model.home.DailyActivity;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.domain.repository.ProfileRepository;

public class RoomProfileRepository implements ProfileRepository {

    private final ProfileDao profileDao;
    private final DailyActivityDao dailyActivityDao;
    private final StreakDayDao streakDayDao;
    private final UserSessionStore userSessionStore;

    public RoomProfileRepository(
            ProfileDao profileDao,
            DailyActivityDao dailyActivityDao,
            StreakDayDao streakDayDao,
            UserSessionStore userSessionStore
    ) {
        this.profileDao = profileDao;
        this.dailyActivityDao = dailyActivityDao;
        this.streakDayDao = streakDayDao;
        this.userSessionStore = userSessionStore;
    }

    @Override
    public ProfileData getProfile() {
        ProfileEntity profile = profileDao.getProfile();
        boolean guestMode = userSessionStore == null || !userSessionStore.isLoggedIn();
        if (profile == null || guestMode) {
            profile = new ProfileEntity(
                    1L,
                    "Guest",
                    "guest@local",
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    false,
                    "Guest mode",
                    "",
                    0,
                    false
            );
        }
        List<DailyActivity> activities = new ArrayList<>();
        if (!guestMode) {
            for (DailyActivityEntity entity : dailyActivityDao.getAllOrdered()) {
                activities.add(new DailyActivity(entity.dayLabel, entity.minutes, entity.highlighted));
            }
        }

        return new ProfileData(
                profile.name,
                profile.email,
                profile.currentStreak,
                profile.longestStreak,
                RepositoryFormatters.formatMinutes(profile.totalStudyMinutes),
                activities,
                RepositoryFormatters.buildStreakSummary(profile, streakDayDao.getAllOrdered()),
                profile.darkModeEnabled,
                profile.notificationsEnabled
        );
    }
}
