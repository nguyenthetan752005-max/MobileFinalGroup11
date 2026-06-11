package com.english.learning.service.impl.user;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.service.user.UserAdministrationService;
import com.english.learning.service.user.UserLifecycleManagementService;
import com.english.learning.service.user.UserProfileManagementService;
import com.english.learning.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileManagementService userProfileManagementService;
    private final UserAdministrationService userAdministrationService;
    private final UserLifecycleManagementService userLifecycleManagementService;

    @Override
    public Optional<User> findById(Long id) {
        return userProfileManagementService.findById(id);
    }

    @Override
    public void updateUsername(Long id, String newUsername) throws Exception {
        userProfileManagementService.updateUsername(id, newUsername);
    }

    @Override
    public void updateAvatarUrl(Long id, String avatarUrl) throws Exception {
        userProfileManagementService.updateAvatarUrl(id, avatarUrl);
    }

    @Override
    public void updateAvatar(Long id, String avatarUrl, String avatarPublicId) throws Exception {
        userProfileManagementService.updateAvatar(id, avatarUrl, avatarPublicId);
    }

    @Override
    public void updateNotificationPreference(Long id, boolean notificationsEnabled, String timezone) throws Exception {
        userProfileManagementService.updateNotificationPreference(id, notificationsEnabled, timezone);
    }

    @Override
    public void adminUpdateBasicInfo(Long id, String username, String avatarUrl, boolean isActive, Role role) throws Exception {
        userAdministrationService.adminUpdateBasicInfo(id, username, avatarUrl, isActive, role);
    }

    @Override
    public void adminUpdatePassword(Long id, String newPassword) throws Exception {
        userAdministrationService.adminUpdatePassword(id, newPassword);
    }

    @Override
    public void softDeleteUser(Long id) {
        userLifecycleManagementService.softDeleteUser(id);
    }

    @Override
    public void hardDeleteUser(Long id) throws Exception {
        userLifecycleManagementService.hardDeleteUser(id);
    }

    @Override
    public void restoreUser(Long id) {
        userLifecycleManagementService.restoreUser(id);
    }

    @Override
    public void updateActiveStatus(Long id, boolean isActive) {
        userAdministrationService.updateActiveStatus(id, isActive);
    }

    @Override
    public void resetAllActiveStatuses() {
        userAdministrationService.resetAllActiveStatuses();
    }
}

