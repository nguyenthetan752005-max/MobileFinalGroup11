package com.english.learning.controller.site;

import com.english.learning.entity.AppSetting;
import com.english.learning.service.settings.AppSettingService;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class SiteGlobalModelAttributes {

    private final AppSettingService appSettingService;

    @ModelAttribute
    public void populateSiteSettings(Model model) {
        AppSetting settings = appSettingService.getSettings();
        model.addAttribute("appSiteName", settings.getSiteName());
        model.addAttribute("appSeoMetaDescription", settings.getSeoMetaDescription());
        model.addAttribute("registrationEnabled", Boolean.TRUE.equals(settings.getAllowUserRegistration()));
        model.addAttribute("appSpeakingPassThreshold", settings.getSpeakingPassThreshold());
    }
}
