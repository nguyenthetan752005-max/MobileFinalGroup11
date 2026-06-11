package com.english.learning.controller.admin;

import com.english.learning.dto.AdminUserProfileViewDTO;
import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.service.admin.AdminUserProfileQueryService;
import com.english.learning.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminManagedProfileController {

    private final UserService userService;
    private final AdminUserProfileQueryService adminUserProfileQueryService;

    @GetMapping("/users/{id}/profile")
    public String adminUserProfile(@PathVariable Long id, HttpSession session, Model model) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Optional<User> targetUser = userService.findById(id);
        if (targetUser.isEmpty()) {
            return "redirect:/admin/dashboard";
        }
        if (targetUser.get().getRole() == Role.ADMIN) {
            if (admin.getId().equals(id)) {
                return "redirect:/admin/profile";
            }
            return "redirect:/admin/admins/" + id + "/profile";
        }

        AdminUserProfileViewDTO view = adminUserProfileQueryService.getUserProfile(id);
        AdminProfileViewSupport.bindProfileModel(model, admin, view, false, true, false, "users");
        return "admin/user-profile";
    }

    @GetMapping("/admins/{id}/profile")
    public String adminAccountProfile(@PathVariable Long id, HttpSession session, Model model) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }
        if (admin.getId().equals(id)) {
            return "redirect:/admin/profile";
        }
        return "redirect:/admin/dashboard?tab=admins";
    }

    @PostMapping("/users/{id}/profile/update")
    public String updateUserProfileByAdmin(@PathVariable Long id,
                                           @RequestParam("username") String username,
                                           @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                           @RequestParam("isActive") boolean isActive,
                                           @RequestParam("role") Role role,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        try {
            ensureManagedUser(id);
            userService.adminUpdateBasicInfo(id, username, avatarUrl, isActive, role);
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat thong tin nguoi dung.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users/" + id + "/profile";
    }

    @PostMapping("/users/{id}/profile/password")
    public String updateUserPasswordByAdmin(@PathVariable Long id,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestParam("confirmPassword") String confirmPassword,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mat khau xac nhan khong khop.");
            return "redirect:/admin/users/" + id + "/profile";
        }

        try {
            ensureManagedUser(id);
            userService.adminUpdatePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat mat khau nguoi dung.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users/" + id + "/profile";
    }

    private void ensureManagedUser(Long id) {
        User targetUser = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nguoi dung."));
        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("Khong duoc dung man quan ly user de chinh sua tai khoan admin.");
        }
    }
}
