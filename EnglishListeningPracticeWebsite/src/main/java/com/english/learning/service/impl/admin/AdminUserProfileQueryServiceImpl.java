package com.english.learning.service.impl.admin;

import com.english.learning.dto.AdminUserProfileViewDTO;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.admin.AdminUserProfileQueryService;
import com.english.learning.service.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminUserProfileQueryServiceImpl implements AdminUserProfileQueryService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final AppSettingService appSettingService;

    @Override
    public AdminUserProfileViewDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        List<UserProgress> recentProgress = userProgressRepository.findTop100ByUser_IdOrderByLastAccessedDesc(userId);
        long completed = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.COMPLETED).count();
        long inProgress = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.IN_PROGRESS).count();
        long skipped = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.SKIPPED).count();

        List<SpeakingResult> speakingResults = speakingResultRepository.findByUser_Id(userId);
        int topScore = speakingResults.stream()
                .map(SpeakingResult::getScore)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        double averageScore = speakingResults.stream()
                .map(SpeakingResult::getScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return AdminUserProfileViewDTO.builder()
                .userDetail(user)
                .recentProgress(recentProgress)
                .progressCompleted(completed)
                .progressInProgress(inProgress)
                .progressSkipped(skipped)
                .topScore(topScore)
                .avgScore(String.format(Locale.US, "%.1f", averageScore))
                .isOnline(user.isOnline(appSettingService.getOnlineTimeoutMinutes()))
                .build();
    }

    @Override
    public AdminUserProfileViewDTO getAdminProfile(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin khong ton tai"));

        return AdminUserProfileViewDTO.builder()
                .userDetail(admin)
                .recentProgress(List.of())
                .progressCompleted(0)
                .progressInProgress(0)
                .progressSkipped(0)
                .topScore(0)
                .avgScore("0.0")
                .isOnline(admin.isOnline(appSettingService.getOnlineTimeoutMinutes()))
                .build();
    }
}

