package com.english.learning.repository;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByRole(Role role);

    // Queries for the Leaderboard
    List<User> findTop30ByRoleOrderByActiveTime7dDesc(Role role);
    List<User> findTop30ByRoleOrderByActiveTime30dDesc(Role role);
    List<User> findByRoleOrderByActiveTime7dDesc(Role role);
    List<User> findByRoleOrderByActiveTime30dDesc(Role role);

    List<User> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE is_deleted = true", nativeQuery = true)
    List<User> findDeletedUsers();

    @org.springframework.data.jpa.repository.Query(value = "SELECT SUM(total_active_time) FROM users WHERE is_deleted = false", nativeQuery = true)
    Long sumTotalActiveTime();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findAnyUserById(@org.springframework.data.repository.query.Param("id") Long id);

    // Separate queries for User/Admin management tabs
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE role = 'USER' AND is_deleted = false", nativeQuery = true)
    List<User> findAllRegularUsers();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE role = 'ADMIN' AND is_deleted = false", nativeQuery = true)
    List<User> findAllAdmins();

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND is_deleted = false", nativeQuery = true)
    long countAdmins();

    @Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE users SET is_active = false", nativeQuery = true)
    void resetAllActiveStatuses();

    @Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE users SET last_active_at = NULL", nativeQuery = true)
    void resetAllOnlinePresence();

    @Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE users SET is_active = true WHERE is_active = false AND suspension_reason IS NULL", nativeQuery = true)
    void repairWronglyDeactivatedUsers();
}
