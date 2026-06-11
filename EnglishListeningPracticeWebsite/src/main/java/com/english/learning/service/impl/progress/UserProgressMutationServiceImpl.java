package com.english.learning.service.impl.progress;

import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.progress.UserProgressMutationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class UserProgressMutationServiceImpl implements UserProgressMutationService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    public UserProgress updateProgress(Long userId, Long sentenceId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);

        if (progressOpt.isPresent()) {
            UserProgress progress = progressOpt.get();
            if (progress.getStatus() == UserProgressStatus.COMPLETED) {
                return progress;
            }
            progress.setStatus(UserProgressStatus.IN_PROGRESS);
            return userProgressRepository.save(progress);
        }

        UserProgress progress = createNewProgress(userId, sentenceId);
        progress.setStatus(UserProgressStatus.IN_PROGRESS);
        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress completeSentence(Long userId, Long sentenceId) {
        UserProgress progress = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId)
                .orElseGet(() -> createNewProgress(userId, sentenceId));
        progress.setStatus(UserProgressStatus.COMPLETED);
        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress skipSentence(Long userId, Long sentenceId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);

        if (progressOpt.isPresent()) {
            UserProgress progress = progressOpt.get();
            if (progress.getStatus() == UserProgressStatus.COMPLETED) {
                return progress;
            }
            progress.setStatus(UserProgressStatus.SKIPPED);
            return userProgressRepository.save(progress);
        }

        UserProgress progress = createNewProgress(userId, sentenceId);
        progress.setStatus(UserProgressStatus.SKIPPED);
        return userProgressRepository.save(progress);
    }

    private UserProgress createNewProgress(Long userId, Long sentenceId) {
        UserProgress progress = new UserProgress();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User khong ton tai."));
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence khong ton tai."));
        progress.setUser(user);
        progress.setSentence(sentence);
        return progress;
    }
}

