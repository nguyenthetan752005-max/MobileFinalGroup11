package com.english.learning.service.impl.progress;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.dto.mobile.MobileSentenceProgressResponse;
import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.service.progress.InProgressLessonQueryService;
import com.english.learning.service.progress.UserProgressMutationService;
import com.english.learning.service.progress.UserProgressQueryService;
import com.english.learning.service.progress.UserProgressService;
import com.english.learning.service.progress.UserProgressStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProgressServiceImpl implements UserProgressService {

    private final UserProgressQueryService userProgressQueryService;
    private final UserProgressMutationService userProgressMutationService;
    private final UserProgressStatusService userProgressStatusService;
    private final InProgressLessonQueryService inProgressLessonQueryService;

    @Override
    public Optional<UserProgress> getProgress(Long userId, Long sentenceId) {
        return userProgressQueryService.getProgress(userId, sentenceId);
    }

    @Override
    public List<UserProgress> getProgressByUserId(Long userId) {
        return userProgressQueryService.getProgressByUserId(userId);
    }

    @Override
    public List<UserProgress> getProgressByUserIdAndLessonId(Long userId, Long lessonId) {
        return userProgressQueryService.getProgressByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public Map<Long, String> getUserProgressMapAsStrings(Long userId, Long lessonId) {
        return userProgressQueryService.getUserProgressMapAsStrings(userId, lessonId);
    }

    @Override
    public List<MobileSentenceProgressResponse> getSentenceProgressSnapshot(Long userId, Long lessonId) {
        return userProgressQueryService.getSentenceProgressSnapshot(userId, lessonId);
    }

    @Override
    public UserProgress updateProgress(Long userId, Long sentenceId) {
        return userProgressMutationService.updateProgress(userId, sentenceId);
    }

    @Override
    public UserProgress completeSentence(Long userId, Long sentenceId) {
        return userProgressMutationService.completeSentence(userId, sentenceId);
    }

    @Override
    public UserProgress skipSentence(Long userId, Long sentenceId) {
        return userProgressMutationService.skipSentence(userId, sentenceId);
    }

    @Override
    public UserProgressStatus getLessonStatus(Long userId, Long lessonId) {
        return userProgressStatusService.getLessonStatus(userId, lessonId);
    }

    @Override
    public UserProgressStatus getSectionStatus(Long userId, Long sectionId) {
        return userProgressStatusService.getSectionStatus(userId, sectionId);
    }

    @Override
    public UserProgressStatus getCategoryStatus(Long userId, Long categoryId) {
        return userProgressStatusService.getCategoryStatus(userId, categoryId);
    }

    @Override
    public List<InProgressLessonDTO> getInProgressLessons(Long userId) {
        return inProgressLessonQueryService.getInProgressLessons(userId);
    }
}

