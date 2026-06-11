package com.english.learning.controller.admin;

import com.english.learning.dto.AdminUserProfileViewDTO;
import com.english.learning.entity.User;
import com.english.learning.service.admin.AdminUserProfileQueryService;
import com.english.learning.service.auth.PasswordResetService;
import com.english.learning.service.user.UserAvatarService;
import com.english.learning.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSelfProfileController {

    private final UserService userService;
    private final AdminUserProfileQueryService adminUserProfileQueryService;
    private final UserAvatarService userAvatarService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/profile")
    public String adminSelfProfile(HttpSession session, Model model) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isEmpty()) {
            return "redirect:/admin/login";
        }

        admin = freshAdmin.get();
        session.setAttribute("loggedInAdmin", admin);
        AdminUserProfileViewDTO view = adminUserProfileQueryService.getAdminProfile(admin.getId());
        AdminProfileViewSupport.bindProfileModel(model, admin, view, true, false, true, "admins");
        return "admin/user-profile";
    }

    @PostMapping("/profile/update")
    public String updateSelfAdminProfile(@RequestParam("username") String username,
                                         @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        try {
            User freshAdmin = userService.findById(admin.getId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan admin."));
            userService.adminUpdateBasicInfo(
                    freshAdmin.getId(),
                    username,
                    avatarUrl,
                    Boolean.TRUE.equals(freshAdmin.getIsActive()),
                    freshAdmin.getRole()
            );
            User updatedAdmin = userService.findById(freshAdmin.getId()).orElse(freshAdmin);
            session.setAttribute("loggedInAdmin", updatedAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat thong tin admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/update-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAdminAvatar(@RequestParam("avatar") MultipartFile file,
                                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            response.put("success", false);
            response.put("message", "Chua dang nhap admin");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Map<String, String> uploadResult = userAvatarService.updateAdminAvatar(admin.getId(), file);
            Optional<User> updatedAdmin = userService.findById(admin.getId());
            updatedAdmin.ifPresent(value -> session.setAttribute("loggedInAdmin", value));
            response.put("success", true);
            response.put("avatarUrl", uploadResult.get("url"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/request-password-change")
    public String requestAdminPasswordChange(HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = AdminProfileViewSupport.getLoggedInAdmin(session);
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay tai khoan admin hien tai.");
            return "redirect:/admin/profile";
        }

        try {
            User targetAdmin = freshAdmin.get();
            String token = passwordResetService.createTokenForUser(targetAdmin);
            passwordResetService.sendAdminPasswordChangeEmail(targetAdmin.getEmail(), token);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Da gui email xac nhan doi mat khau admin. Sau khi doi thanh cong, ban se bi dang xuat."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Khong the gui email doi mat khau admin. Vui long thu lai sau."
            );
        }
        return "redirect:/admin/profile";
    }
}
