package com.english.learning.controller.auth;

import com.english.learning.entity.User;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AppSettingService appSettingService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!appSettingService.isUserRegistrationAllowed()) {
            model.addAttribute("error", "User registration is currently disabled.");
            return "auth/login";
        }
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {
        if (!appSettingService.isUserRegistrationAllowed()) {
            model.addAttribute("error", "User registration is currently disabled.");
            return "auth/login";
        }
        try {
            authService.register(user);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            Model model, HttpSession session) {
        Optional<User> userOpt = authService.authenticateUser(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userService.updateActiveStatus(user.getId(), true);
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            userService.updateActiveStatus(loggedInUser.getId(), false);
        }
        session.removeAttribute("loggedInUser");
        return "redirect:/login";
    }
}

