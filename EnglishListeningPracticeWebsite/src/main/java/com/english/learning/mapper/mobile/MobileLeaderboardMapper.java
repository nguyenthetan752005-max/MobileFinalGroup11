package com.english.learning.mapper.mobile;

import com.english.learning.dto.mobile.MobileLeaderboardEntryResponse;
import com.english.learning.dto.mobile.MobileLeaderboardResponse;
import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MobileLeaderboardMapper {

    private static final int LEADERBOARD_LIMIT = 30;

    private final UserRepository userRepository;

    @Cacheable(value = "leaderboard", key = "#currentUserId != null ? #currentUserId : 'guest'", unless = "#result == null")
    public MobileLeaderboardResponse buildLeaderboard(Long currentUserId) {
        List<User> weeklyAllUsers = userRepository.findTop30ByRoleOrderByActiveTime7dDesc(Role.USER);
        List<User> monthlyAllUsers = userRepository.findTop30ByRoleOrderByActiveTime30dDesc(Role.USER);

        List<MobileLeaderboardEntryResponse> weeklyEntries = buildEntries(weeklyAllUsers, true, currentUserId);
        List<MobileLeaderboardEntryResponse> monthlyEntries = buildEntries(monthlyAllUsers, false, currentUserId);

        Integer currentUserRank = 0;
        String currentUserTime = "Guest mode";

        if (currentUserId != null) {
            currentUserRank = resolveRank(weeklyAllUsers, currentUserId);
            currentUserTime = resolveCurrentUserTime(currentUserId, true);
        }

        return MobileLeaderboardResponse.builder()
                .weeklyEntries(weeklyEntries)
                .monthlyEntries(monthlyEntries)
                .currentUserRank(currentUserRank)
                .currentUserTime(currentUserTime)
                .build();
    }

    private List<MobileLeaderboardEntryResponse> buildEntries(List<User> users, boolean is7Day, Long currentUserId) {
        List<MobileLeaderboardEntryResponse> entries = new ArrayList<>();
        int limit = Math.min(users.size(), LEADERBOARD_LIMIT);
        for (int i = 0; i < limit; i++) {
            User user = users.get(i);
            int seconds = is7Day
                    ? (user.getActiveTime7d() != null ? user.getActiveTime7d() : 0)
                    : (user.getActiveTime30d() != null ? user.getActiveTime30d() : 0);
            String username = user.getUsername() == null || user.getUsername().isBlank() ? "User" : user.getUsername().trim();
            entries.add(MobileLeaderboardEntryResponse.builder()
                    .rank(i + 1)
                    .name(username)
                    .avatarLabel(username.substring(0, 1).toUpperCase())
                    .activeTime(formatActiveTimeForMobile(seconds))
                    .currentUser(currentUserId != null && currentUserId.equals(user.getId()))
                    .build());
        }
        return entries;
    }

    private int resolveRank(List<User> sortedUsers, Long currentUserId) {
        // Only search in top 30 (if not found, user is not in top 30)
        for (int i = 0; i < sortedUsers.size(); i++) {
            if (currentUserId.equals(sortedUsers.get(i).getId())) {
                return i + 1;
            }
        }
        // User not in top 30, query full list to find actual rank
        List<User> allUsers = userRepository.findByRoleOrderByActiveTime7dDesc(Role.USER);
        for (int i = 0; i < allUsers.size(); i++) {
            if (currentUserId.equals(allUsers.get(i).getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    private String resolveCurrentUserTime(Long currentUserId, boolean is7Day) {
        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return "0m";
        }
        User user = userOptional.get();
        Integer seconds = is7Day ? user.getActiveTime7d() : user.getActiveTime30d();
        return formatActiveTimeForMobile(seconds != null ? seconds : 0);
    }

    private String formatActiveTimeForMobile(int totalSeconds) {
        int minutes = totalSeconds / 60;
        if (minutes < 1) {
            return "0m";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            return hours + "h " + remainingMinutes + "m";
        }
        return remainingMinutes + "m";
    }
}
