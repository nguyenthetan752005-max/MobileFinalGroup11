package com.english.learning.service.user;

import com.english.learning.enums.Role;

public interface UserAdministrationService {
    void adminUpdateBasicInfo(Long id, String username, String avatarUrl, boolean isActive, Role role) throws Exception;

    void adminUpdatePassword(Long id, String newPassword) throws Exception;

    void updateActiveStatus(Long id, boolean isActive);

    void resetAllActiveStatuses();
}

