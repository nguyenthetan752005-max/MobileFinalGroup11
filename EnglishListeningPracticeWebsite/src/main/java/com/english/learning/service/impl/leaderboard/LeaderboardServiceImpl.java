package com.english.learning.service.impl.leaderboard;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.DailyStudyStatisticRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.leaderboard.LeaderboardService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Design Logic: Leaderboard Service Implementation (SOLID SRP + OCP).
 *
 * - refreshLeaderboardCaches(): Scheduled Cron Job (every hour).
 *   Aggregates daily_study_statistics â†’ caches SUM into User.activeTime7d/30d.
 *   Uses saveAll() for batch DB efficiency instead of save() in a loop.
 *
 * - getTopUsers7Days/30Days(): Returns List<Map> of pre-formatted row data
 *   for the View layer. This keeps the Controller thin (MVC compliance)
 *   and avoids leaking Entity objects to the template.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    private final UserRepository userRepository;
    private final DailyStudyStatisticRepository dailyRepo;

    @Override
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void refreshLeaderboardCaches() {
        log.info("Scheduled: Refreshing Leaderboard 7D and 30D caches...");

        List<User> allUsers = userRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        for (User user : allUsers) {
            Integer time7d = dailyRepo.sumActiveTimeByUserAndStudyDateAfter(user, sevenDaysAgo);
            Integer time30d = dailyRepo.sumActiveTimeByUserAndStudyDateAfter(user, thirtyDaysAgo);
            user.setActiveTime7d(time7d != null ? time7d : 0);
            user.setActiveTime30d(time30d != null ? time30d : 0);
        }

        // Batch save for Performance (SOLID: optimize DB writes)
        userRepository.saveAll(allUsers);
        log.info("Completed: Refreshed Leaderboard caches for {} users.", allUsers.size());
    }

    @Override
    public List<Map<String, Object>> getTopUsers7Days() {
        return buildLeaderboardRows(userRepository.findTop30ByRoleOrderByActiveTime7dDesc(Role.USER), true);
    }

    @Override
    public List<Map<String, Object>> getTopUsers30Days() {
        return buildLeaderboardRows(userRepository.findTop30ByRoleOrderByActiveTime30dDesc(Role.USER), false);
    }

    /**
     * Converts User entities into template-ready Map rows.
     * Pre-formats display strings so the View (Thymeleaf) stays logic-free.
     */
    private List<Map<String, Object>> buildLeaderboardRows(List<User> users, boolean is7Day) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("rank", i + 1);
            row.put("username", u.getUsername());
            row.put("initial", u.getUsername().substring(0, 1).toUpperCase());
            row.put("avatarColor", "hsl(" + ((i + 1) * 137 % 360) + ", 70%, 50%)");

            int seconds = is7Day
                    ? (u.getActiveTime7d() != null ? u.getActiveTime7d() : 0)
                    : (u.getActiveTime30d() != null ? u.getActiveTime30d() : 0);
            row.put("activeTime", TimeFormatUtil.formatActiveTime(seconds));
            rows.add(row);
        }
        return rows;
    }
}

