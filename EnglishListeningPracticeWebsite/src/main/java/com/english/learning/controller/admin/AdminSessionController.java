package com.english.learning.controller.admin;

import com.english.learning.entity.User;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSessionController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/login")
    public String adminLogin(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String doAdminLogin(@RequestParam(value = "username", required = false) String username,
                               @RequestParam(value = "password", required = false) String password,
                               Model model, HttpSession session) {
        Optional<User> adminOpt = authService.authenticateAdmin(username, password);
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            userService.updateActiveStatus(admin.getId(), true);
            session.setAttribute("loggedInAdmin", admin);
            session.setAttribute("forceAdminOverviewTab", true);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Invalid credentials or insufficient permissions!");
        return "admin/login";
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin != null) {
            userService.updateActiveStatus(admin.getId(), false);
        }
        session.removeAttribute("loggedInAdmin");
        return "redirect:/admin/login";
    }
}
