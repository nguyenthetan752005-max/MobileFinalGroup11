package com.english.learning.controller.user;

import com.english.learning.entity.User;
import com.english.learning.service.auth.PasswordResetService;
import com.english.learning.service.user.UserAvatarService;
import com.english.learning.service.user.UserService;
import com.english.learning.util.TimeFormatUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserAvatarService userAvatarService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(loggedInUser.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            String formattedTime = TimeFormatUtil.formatActiveTime(user.getTotalActiveTime() != null ? user.getTotalActiveTime() : 0);
            model.addAttribute("formattedActiveTime", formattedTime);
            return "user/profile";
        }

        return "redirect:/login";
    }

    @PostMapping("/profile/update-name")
    public String updateUsername(@RequestParam("newUsername") String newUsername,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            userService.updateUsername(loggedInUser.getId(), newUsername);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat ten thanh cong!");
            loggedInUser.setUsername(newUsername == null ? null : newUsername.trim());
            session.setAttribute("loggedInUser", loggedInUser);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/update-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAvatar(@RequestParam("avatar") MultipartFile file,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "Chua dang nhap");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Map<String, String> uploadResult = userAvatarService.updateUserAvatar(loggedInUser.getId(), file);
            String avatarUrl = uploadResult.get("url");
            String avatarPublicId = uploadResult.get("publicId");

            loggedInUser.setAvatarUrl(avatarUrl);
            loggedInUser.setAvatarPublicId(avatarPublicId);
            session.setAttribute("loggedInUser", loggedInUser);

            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/request-password-change")
    public String requestPasswordChange(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(loggedInUser.getId());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay tai khoan hien tai.");
            return "redirect:/profile";
        }

        try {
            User user = userOpt.get();
            String token = passwordResetService.createTokenForUser(user);
            passwordResetService.sendProfilePasswordChangeEmail(user.getEmail(), token);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Da gui email xac nhan doi mat khau. Sau khi doi thanh cong, ban se bi dang xuat."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Khong the gui email doi mat khau. Vui long thu lai sau."
            );
        }

        return "redirect:/profile";
    }
}
