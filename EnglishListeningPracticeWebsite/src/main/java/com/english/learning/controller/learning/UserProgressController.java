package com.english.learning.controller.learning;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.entity.UserProgress;
import com.english.learning.service.progress.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;

    @GetMapping("/api/progress/in-progress")
    @ResponseBody
    public List<InProgressLessonDTO> getInProgressLessons(@RequestParam Long userId) {
        return userProgressService.getInProgressLessons(userId);
    }

    @PostMapping("/progress/update")
    @ResponseBody
    public UserProgress updateProgress(
            @RequestParam Long userId,
            @RequestParam Long sentenceId) {
        return userProgressService.updateProgress(userId, sentenceId);
    }

    @PostMapping("/progress/complete")
    @ResponseBody
    public UserProgress completeSentence(
            @RequestParam Long userId,
            @RequestParam Long sentenceId) {
        return userProgressService.completeSentence(userId, sentenceId);
    }

    @PostMapping("/progress/skip")
    @ResponseBody
    public UserProgress skipSentence(
            @RequestParam Long userId,
            @RequestParam Long sentenceId) {
        return userProgressService.skipSentence(userId, sentenceId);
    }
}
