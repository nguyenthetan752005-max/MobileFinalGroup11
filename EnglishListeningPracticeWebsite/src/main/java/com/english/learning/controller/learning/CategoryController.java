package com.english.learning.controller.learning;

import com.english.learning.entity.Category;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.progress.UserProgressService;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.enums.PracticeType;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.entity.User;
import com.english.learning.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserProgressService userProgressService;

    @GetMapping("/exercises")
    public String showAllTopics(Model model, HttpSession session) {
        List<Category> categories = categoryService.getPublishedCategoriesByPracticeType(PracticeType.LISTENING);
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
        return "exercises/categories-list";
    }

    @GetMapping("/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model, HttpSession session) {
        Category category = categoryService.getPublishedCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại!"));

        User user = (User) session.getAttribute("loggedInUser");

        List<SectionWithLessonsDTO> sectionDtos = categoryService.getSectionWithLessonsDTOs(id, user, PracticeType.LISTENING);
        List<String> expandedLevels = categoryService.getExpandedLevels(category.getLevelRange());

        model.addAttribute("category", category);
        model.addAttribute("sections", sectionDtos);
        model.addAttribute("levels", expandedLevels);

        return "exercises/sections";
    }
}
