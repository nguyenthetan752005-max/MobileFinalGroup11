package com.english.learning.service.user;

import com.english.learning.entity.User;

import java.util.Optional;

/**
 * SRP: User Management Service Interface (OOSE / SOLID).
 *
 * Handles ONLY user profile and account management concerns:
 * - Profile updates (username, avatar)
 * - Admin moderation (role changes, status toggles)
 * - User lifecycle (soft delete, hard delete, restore)
 *
 * Authentication logic (register, login, password) belongs to AuthService.
 */
public interface UserService {
    Optional<User> findById(Long id);
    void updateUsername(Long id, String newUsername) throws Exception;
    void updateAvatarUrl(Long id, String avatarUrl) throws Exception;
    void updateAvatar(Long id, String avatarUrl, String avatarPublicId) throws Exception;
    void updateNotificationPreference(Long id, boolean notificationsEnabled, String timezone) throws Exception;

    void adminUpdateBasicInfo(Long id, String username, String avatarUrl, boolean isActive, com.english.learning.enums.Role role) throws Exception;
    void adminUpdatePassword(Long id, String newPassword) throws Exception;
    void softDeleteUser(Long id);
    void hardDeleteUser(Long id) throws Exception;
    void restoreUser(Long id);
    void updateActiveStatus(Long id, boolean isActive);
    void resetAllActiveStatuses();
}

