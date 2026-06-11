package com.english.learning.service.mobile;

import com.english.learning.dto.mobile.MobileBootstrapResponse;

/**
 * Service Layer: Mobile Bootstrap API (SOLID SRP).
 * Sole responsibility: Aggregate and return all published content
 * for Android app initial sync via /api/mobile/bootstrap.
 */
public interface MobileBootstrapService {
    MobileBootstrapResponse getBootstrapData();
}
