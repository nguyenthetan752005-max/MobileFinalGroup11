package com.english.learning.controller.admin;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.User;
import com.english.learning.service.settings.AppSettingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AppSettingService appSettingService;

    @PostMapping("/settings")
    public String updateSettings(@Valid @ModelAttribute("settingForm") AppSettingForm settingForm,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.settingForm", bindingResult);
            redirectAttributes.addFlashAttribute("settingForm", settingForm);
            redirectAttributes.addFlashAttribute(
                    "settingsError",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
            return "redirect:/admin/dashboard?tab=settings";
        }

        appSettingService.updateSettings(settingForm);
        redirectAttributes.addFlashAttribute("settingsSuccess", "Settings updated successfully.");
        return "redirect:/admin/dashboard?tab=settings";
    }
}
