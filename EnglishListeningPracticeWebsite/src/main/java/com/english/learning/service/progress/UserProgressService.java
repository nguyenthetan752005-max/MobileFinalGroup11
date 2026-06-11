package com.english.learning.service.progress;

import com.english.learning.entity.UserProgress;

import com.english.learning.enums.UserProgressStatus;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.dto.mobile.MobileSentenceProgressResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserProgressService {
    Optional<UserProgress> getProgress(Long userId, Long sentenceId);
    List<UserProgress> getProgressByUserId(Long userId);
    List<UserProgress> getProgressByUserIdAndLessonId(Long userId, Long lessonId);
    Map<Long, String> getUserProgressMapAsStrings(Long userId, Long lessonId);
    List<MobileSentenceProgressResponse> getSentenceProgressSnapshot(Long userId, Long lessonId);
    UserProgress updateProgress(Long userId, Long sentenceId);
    UserProgress completeSentence(Long userId, Long sentenceId);
    UserProgress skipSentence(Long userId, Long sentenceId);
    
    // Status calculations
    UserProgressStatus getLessonStatus(Long userId, Long lessonId);
    UserProgressStatus getSectionStatus(Long userId, Long sectionId);
    UserProgressStatus getCategoryStatus(Long userId, Long categoryId);
    
    // Get in-progress lessons for user
    List<InProgressLessonDTO> getInProgressLessons(Long userId);
}

