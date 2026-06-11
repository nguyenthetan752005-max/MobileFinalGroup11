package com.english.learning.controller.learning;

import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.content.lesson.LessonService;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.enums.PracticeType;
import com.english.learning.service.progress.UserProgressService;
import com.english.learning.service.content.sentence.SentenceService;
import com.english.learning.service.learning.hint.HintService;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.util.AudioUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SpeakingExerciseController {

    private final CategoryService categoryService;
    private final LessonService lessonService;
    private final UserProgressService userProgressService;
    private final SentenceService sentenceService;
    private final HintService hintService;
    private final AudioUrlResolver audioUrlResolver;

    @GetMapping("/speaking-exercises")
    public String showAllTopics(Model model, HttpSession session) {
        List<Category> categories = categoryService
                .getPublishedCategoriesByPracticeType(PracticeType.SPEAKING);
        User user = (User) session.getAttribute("loggedInUser");

        Map<Long, UserProgressStatus> categoryStatuses = new HashMap<>();
        if (user != null) {
            for (Category cat : categories) {
                UserProgressStatus status = userProgressService.getCategoryStatus(user.getId(), cat.getId());
                if (status != null) {
                    categoryStatuses.put(cat.getId(), status);
                }
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("categoryStatuses", categoryStatuses);
        return "speaking/categories-list";
    }

    @GetMapping("/speaking/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model, HttpSession session) {
        Category category = categoryService.getPublishedCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại!"));

        User user = (User) session.getAttribute("loggedInUser");

        List<SectionWithLessonsDTO> sectionDtos = categoryService.getSectionWithLessonsDTOs(id, user, PracticeType.SPEAKING);
        List<String> expandedLevels = categoryService.getExpandedLevels(category.getLevelRange());

        model.addAttribute("category", category);
        model.addAttribute("sections", sectionDtos);
        model.addAttribute("levels", expandedLevels);

        return "speaking/sections";
    }



    @GetMapping("/speaking/lesson/{id}")
    public String getSpeakingPractice(@PathVariable Long id,
            @RequestParam(required = false) Integer sentenceIndex,
            Model model, HttpSession session) {
        Lesson lesson = lessonService.getLessonById(id)
                .flatMap(candidate -> lessonService.getPublishedLessonById(candidate.getId()))
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));

        List<Sentence> sentences = sentenceService.getPublishedSentencesByLessonId(id);

        // Dùng HintService để lấy map
        Map<Long, List<String>> hintsMap = hintService.getHintsMap(sentences);

        // Map lưu trạng thái hoàn thành (COMPLETED/IN_PROGRESS/SKIPPED)
        Map<Long, String> userProgressMap = new HashMap<>();
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            userProgressMap = userProgressService.getUserProgressMapAsStrings(user.getId(), id);
        }

        Map<Long, String> sentenceAudioUrls = new HashMap<>();
        for (Sentence s : sentences) {
            String url = audioUrlResolver.resolve(s);
            if (url != null) sentenceAudioUrls.put(s.getId(), url);
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("sentences", sentences);
        model.addAttribute("sentenceAudioUrls", sentenceAudioUrls);
        model.addAttribute("hintsMap", hintsMap); // Truyền map này xuống Thymeleaf
        model.addAttribute("userProgressMap", userProgressMap);
        model.addAttribute("category", lesson.getSection().getCategory());

        LessonNavigationDTO navigation = lessonService.getLessonNavigation(lesson, PracticeType.SPEAKING);
        model.addAttribute("section", lesson.getSection());
        model.addAttribute("nextLesson", navigation.getNextLesson());
        model.addAttribute("isLastLessonInSection", navigation.isLastLessonInSection());
        model.addAttribute("initialSentenceIndex", sentenceIndex != null ? sentenceIndex : 0);

        return "speaking/speaking-practice";
    }
}
