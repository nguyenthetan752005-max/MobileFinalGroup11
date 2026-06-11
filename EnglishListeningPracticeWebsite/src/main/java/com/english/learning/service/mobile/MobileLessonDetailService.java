package com.english.learning.service.mobile;

import com.english.learning.dto.mobile.MobileLessonDetailResponse;

/**
 * Service: Lesson Detail API.
 * Returns lesson metadata + all published sentences for a single lesson.
 */
public interface MobileLessonDetailService {
    MobileLessonDetailResponse getLessonDetail(Long lessonId);
}
