package com.english.learning.service.impl.progress;

import com.english.learning.enums.UserProgressStatus;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.progress.UserProgressStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProgressStatusServiceImpl implements UserProgressStatusService {

    private final UserProgressRepository userProgressRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    public UserProgressStatus getLessonStatus(Long userId, Long lessonId) {
        long totalSentences = sentenceRepository.countByLesson_Id(lessonId);
        if (totalSentences == 0) {
            return null;
        }

        long completedSentences =
                userProgressRepository.countByUserIdAndLessonIdAndStatus(userId, lessonId, UserProgressStatus.COMPLETED);
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndLessonId(userId, lessonId);
        return anyProgressCount > 0 ? UserProgressStatus.IN_PROGRESS : null;
    }

    @Override
    public UserProgressStatus getSectionStatus(Long userId, Long sectionId) {
        long totalSentences = sentenceRepository.countByLesson_Section_Id(sectionId);
        if (totalSentences == 0) {
            return null;
        }

        long completedSentences =
                userProgressRepository.countByUserIdAndSectionIdAndStatus(userId, sectionId, UserProgressStatus.COMPLETED);
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndSectionId(userId, sectionId);
        return anyProgressCount > 0 ? UserProgressStatus.IN_PROGRESS : null;
    }

    @Override
    public UserProgressStatus getCategoryStatus(Long userId, Long categoryId) {
        long totalSentences = sentenceRepository.countByLesson_Section_Category_Id(categoryId);
        if (totalSentences == 0) {
            return null;
        }

        long completedSentences =
                userProgressRepository.countByUserIdAndCategoryIdAndStatus(userId, categoryId, UserProgressStatus.COMPLETED);
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndCategoryId(userId, categoryId);
        return anyProgressCount > 0 ? UserProgressStatus.IN_PROGRESS : null;
    }
}

