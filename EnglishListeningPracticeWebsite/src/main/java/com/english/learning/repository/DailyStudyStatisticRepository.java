package com.english.learning.repository;

import com.english.learning.entity.DailyStudyStatistic;
import com.english.learning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStudyStatisticRepository extends JpaRepository<DailyStudyStatistic, Long> {
    
    // Finds the record for today so the tracking service can accumulate seconds
    Optional<DailyStudyStatistic> findByUserAndStudyDate(User user, LocalDate studyDate);

    // Sums up the active time for rolling windows to be cached into User
    @Query("SELECT COALESCE(SUM(d.activeTimeSeconds), 0) FROM DailyStudyStatistic d WHERE d.user = :user AND d.studyDate >= :startDate")
    Integer sumActiveTimeByUserAndStudyDateAfter(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // Last 7 days of study data for weekly activity chart
    java.util.List<DailyStudyStatistic> findByUserAndStudyDateBetweenOrderByStudyDateAsc(User user, LocalDate startDate, LocalDate endDate);

    // All study dates for streak computation (ordered newest first)
    @Query("SELECT DISTINCT d.studyDate FROM DailyStudyStatistic d WHERE d.user = :user AND d.activeTimeSeconds > 0 ORDER BY d.studyDate DESC")
    java.util.List<LocalDate> findDistinctStudyDatesByUserOrderByDesc(@Param("user") User user);

    @Modifying
    @Query(value = "DELETE FROM daily_study_statistics WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
}
