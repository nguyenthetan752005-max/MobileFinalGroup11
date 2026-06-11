package com.english.learning.service.mobile;

import com.english.learning.dto.mobile.MobileBootstrapLiteResponse;

/**
 * Service: Bootstrap Lite API.
 * Returns catalog data WITHOUT sentences for lighter initial sync.
 */
public interface MobileBootstrapLiteService {
    MobileBootstrapLiteResponse getBootstrapLiteData();
}
