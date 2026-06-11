package com.english.learning.service.admin;

import com.english.learning.dto.AdminDashboardDTO;

/**
 * Service interface for Admin Dashboard data aggregation.
 * Follows DIP: Controller depends on this interface, not on concrete repositories.
 */
public interface AdminDashboardService {
    AdminDashboardDTO getDashboardData();
}

