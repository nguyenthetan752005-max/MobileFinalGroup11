package com.english.learning.repository;

import com.english.learning.entity.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByUser_IdOrderByEventAtDesc(Long userId, Pageable pageable);

    long countByUser_IdAndReadAtIsNull(Long userId);

    Optional<UserNotification> findByIdAndUser_Id(Long notificationId, Long userId);

    Optional<UserNotification> findByUser_IdAndDedupKey(Long userId, String dedupKey);

    @Modifying
    @Query("""
            update UserNotification notification
               set notification.readAt = :readAt
             where notification.user.id = :userId
               and notification.readAt is null
            """)
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
