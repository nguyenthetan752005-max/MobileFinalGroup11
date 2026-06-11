package com.english.learning.controller.admin;

import com.english.learning.dto.AdminUserProfileViewDTO;
import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

final class AdminProfileViewSupport {

    private AdminProfileViewSupport() {
    }

    static User getLoggedInAdmin(HttpSession session) {
        return (User) session.getAttribute("loggedInAdmin");
    }

    static void bindProfileModel(Model model,
                                 User admin,
                                 AdminUserProfileViewDTO view,
                                 boolean isSelfAdmin,
                                 boolean canEditUser,
                                 boolean canEditSelfAdmin,
                                 String returnTab) {
        model.addAttribute("admin", admin);
        model.addAttribute("userDetail", view.getUserDetail());
        model.addAttribute("recentProgress", view.getRecentProgress());
        model.addAttribute("progressCompleted", view.getProgressCompleted());
        model.addAttribute("progressInProgress", view.getProgressInProgress());
        model.addAttribute("progressSkipped", view.getProgressSkipped());
        model.addAttribute("topScore", view.getTopScore());
        model.addAttribute("avgScore", view.getAvgScore());
        model.addAttribute("roles", Role.values());
        model.addAttribute("isSelfAdmin", isSelfAdmin);
        model.addAttribute("canEditUser", canEditUser);
        model.addAttribute("canEditSelfAdmin", canEditSelfAdmin);
        model.addAttribute("returnTab", returnTab);
    }
}
