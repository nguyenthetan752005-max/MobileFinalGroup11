package com.english.learning.controller.admin;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.entity.User;
import com.english.learning.service.admin.AdminDashboardService;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final AdminDashboardService adminDashboardService;
    private final AppSettingService appSettingService;

    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(value = "tab", required = false) String initialTab,
                                 HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isPresent()) {
            admin = freshAdmin.get();
            session.setAttribute("loggedInAdmin", admin);
        }

        AdminDashboardDTO dashboard = adminDashboardService.getDashboardData();

        model.addAttribute("totalUsers", dashboard.getTotalUsers());
        model.addAttribute("totalAdmins", dashboard.getTotalAdmins());
        model.addAttribute("totalLessons", dashboard.getTotalLessons());
        model.addAttribute("totalTime", dashboard.getFormattedTotalTime());
        model.addAttribute("recentUsers", dashboard.getRecentUsers());
        model.addAttribute("regularUsers", dashboard.getRegularUsers());
        model.addAttribute("userTopScoreMap", dashboard.getUserTopScoreMap());
        model.addAttribute("categories", dashboard.getCategories());
        model.addAttribute("categoryLevelsMap", dashboard.getCategoryLevelsMap());
        model.addAttribute("categoryLevelOptions", dashboard.getCategoryLevelOptions());
        model.addAttribute("lessonLevelOptions", dashboard.getLessonLevelOptions());
        model.addAttribute("deletedUsers", dashboard.getDeletedUsers());
        model.addAttribute("deletedCategories", dashboard.getDeletedCategories());
        model.addAttribute("deletedSections", dashboard.getDeletedSections());
        model.addAttribute("deletedLessons", dashboard.getDeletedLessons());
        model.addAttribute("deletedSentences", dashboard.getDeletedSentences());
        model.addAttribute("deletedComments", dashboard.getDeletedComments());
        model.addAttribute("deletedSlideshows", dashboard.getDeletedSlideshows());
        model.addAttribute("recentComments", dashboard.getRecentComments());
        model.addAttribute("slideshows", dashboard.getSlideshows());
        model.addAttribute("admin", admin);
        model.addAttribute("initialTab", initialTab);
        if (!model.containsAttribute("settingForm")) {
            model.addAttribute("settingForm", appSettingService.getSettingForm());
        }

        boolean forceAdminOverviewTab = Boolean.TRUE.equals(session.getAttribute("forceAdminOverviewTab"));
        model.addAttribute("forceAdminOverviewTab", forceAdminOverviewTab);
        if (forceAdminOverviewTab) {
            session.removeAttribute("forceAdminOverviewTab");
        }

        return "admin/dashboard";
    }
}
