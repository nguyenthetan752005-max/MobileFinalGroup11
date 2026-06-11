package com.english.learning.service.impl.progress;

import com.english.learning.dto.mobile.MobileSentenceProgressResponse;
import com.english.learning.entity.UserProgress;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.progress.UserProgressQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressQueryServiceImpl implements UserProgressQueryService {

    private final UserProgressRepository userProgressRepository;

    @Override
    public Optional<UserProgress> getProgress(Long userId, Long sentenceId) {
        return userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);
    }

    @Override
    public List<UserProgress> getProgressByUserId(Long userId) {
        return userProgressRepository.findByUser_Id(userId);
    }

    @Override
    public List<UserProgress> getProgressByUserIdAndLessonId(Long userId, Long lessonId) {
        return userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public Map<Long, String> getUserProgressMapAsStrings(Long userId, Long lessonId) {
        return getProgressByUserIdAndLessonId(userId, lessonId).stream()
                .collect(Collectors.toMap(
                        progress -> progress.getSentence().getId(),
                        progress -> progress.getStatus().name()
                ));
    }

    @Override
    public List<MobileSentenceProgressResponse> getSentenceProgressSnapshot(Long userId, Long lessonId) {
        List<UserProgress> progressList = lessonId == null
                ? userProgressRepository.findByUser_Id(userId)
                : userProgressRepository.findByUserIdAndLessonId(userId, lessonId);

        return progressList.stream()
                .filter(progress -> progress.getSentence() != null
                        && progress.getSentence().getLesson() != null
                        && progress.getStatus() != null)
                .map(progress -> MobileSentenceProgressResponse.builder()
                        .sentenceId(progress.getSentence().getId())
                        .lessonId(progress.getSentence().getLesson().getId())
                        .status(progress.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }
}

