package com.english.learning.service.impl.progress;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.UserProgress;
import com.english.learning.enums.PracticeType;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.progress.InProgressLessonQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InProgressLessonQueryServiceImpl implements InProgressLessonQueryService {

    private final UserProgressRepository userProgressRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<InProgressLessonDTO> getInProgressLessons(Long userId) {
        List<InProgressLessonDTO> result = new ArrayList<>();
        List<Long> lessonIdsWithProgress = userProgressRepository.findLessonIdsWithProgressByUserId(userId);

        for (Long lessonId : lessonIdsWithProgress) {
            long totalSentences = sentenceRepository.countByLesson_Id(lessonId);
            if (totalSentences == 0) {
                continue;
            }

            long completedCount =
                    userProgressRepository.countByUserIdAndLessonIdAndStatus(userId, lessonId, UserProgressStatus.COMPLETED);
            if (completedCount == totalSentences) {
                continue;
            }

            Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
            if (lessonOpt.isEmpty()) {
                continue;
            }

            Lesson lesson = lessonOpt.get();
            Section section = lesson.getSection();
            Category category = section != null ? section.getCategory() : null;

            List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
            List<UserProgress> progressList = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
            Map<Long, UserProgressStatus> progressMap = progressList.stream()
                    .collect(Collectors.toMap(progress -> progress.getSentence().getId(), UserProgress::getStatus));

            Long firstUncompletedSentenceId = null;
            int firstUncompletedIndex = 0;
            for (int i = 0; i < sentences.size(); i++) {
                Sentence sentence = sentences.get(i);
                UserProgressStatus status = progressMap.get(sentence.getId());
                if (status != UserProgressStatus.COMPLETED) {
                    firstUncompletedSentenceId = sentence.getId();
                    firstUncompletedIndex = i;
                    break;
                }
            }

            PracticeType practiceType = category != null && category.getPracticeType() != null
                    ? category.getPracticeType()
                    : PracticeType.LISTENING;

            result.add(InProgressLessonDTO.builder()
                    .lessonId(lessonId)
                    .lessonTitle(lesson.getTitle())
                    .categoryName(category != null ? category.getName() : "")
                    .sectionName(section != null ? section.getName() : "")
                    .practiceType(practiceType.name())
                    .totalSentences((int) totalSentences)
                    .completedSentences((int) completedCount)
                    .progressPercent((int) (completedCount * 100 / totalSentences))
                    .firstUncompletedSentenceId(firstUncompletedSentenceId)
                    .firstUncompletedSentenceIndex(firstUncompletedIndex)
                    .build());
        }

        return result;
    }
}

