package com.english.learning.service.admin;

import com.english.learning.dto.AdminUserProfileViewDTO;

public interface AdminUserProfileQueryService {
    AdminUserProfileViewDTO getUserProfile(Long userId);

    AdminUserProfileViewDTO getAdminProfile(Long adminId);
}

