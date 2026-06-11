package com.english.learning.service.impl.tracking;

import com.english.learning.entity.DailyStudyStatistic;
import com.english.learning.entity.User;
import com.english.learning.repository.DailyStudyStatisticRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.tracking.StudyTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyTrackingServiceImpl implements StudyTrackingService {

    private final DailyStudyStatisticRepository dailyRepo;
    private final UserRepository userRepository;

    // Anti-cheat / Anti-AFK guard: Maximum 120 seconds permitted per submitted sentence action.
    // If a user idles for 2 hours before submitting, we only record 2 minutes.
    private static final int MAX_SECONDS_PER_ACTION = 120; 

    @Override
    @Transactional
    @CacheEvict(value = "leaderboard", allEntries = true)
    public void addActiveSeconds(String username, int seconds) {
        if (seconds <= 0) return;

        // 1. Guard against AFK / Cheating
        int finalSeconds = Math.min(seconds, MAX_SECONDS_PER_ACTION);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        LocalDate today = LocalDate.now();

        // 2. Accumulate in daily history table (for Leaderboard sliding windows)
        DailyStudyStatistic dailyStat = dailyRepo.findByUserAndStudyDate(user, today)
                .orElseGet(() -> {
                    DailyStudyStatistic newStat = new DailyStudyStatistic();
                    newStat.setUser(user);
                    newStat.setStudyDate(today);
                    return newStat;
                });

        dailyStat.setActiveTimeSeconds(dailyStat.getActiveTimeSeconds() + finalSeconds);
        dailyRepo.save(dailyStat);

        // 3. Update all-time and cached totals in User table (for real-time leaderboard stats)
        user.setTotalActiveTime((user.getTotalActiveTime() != null ? user.getTotalActiveTime() : 0) + finalSeconds);
        user.setActiveTime7d((user.getActiveTime7d() != null ? user.getActiveTime7d() : 0) + finalSeconds);
        user.setActiveTime30d((user.getActiveTime30d() != null ? user.getActiveTime30d() : 0) + finalSeconds);
        
        userRepository.save(user);
        
        log.info("Action-based tracking: Added {} seconds to user {} for date {}. (Requested: {})", finalSeconds, username, today, seconds);
    }
}

