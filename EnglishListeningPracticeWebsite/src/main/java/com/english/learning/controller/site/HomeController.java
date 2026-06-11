package com.english.learning.controller.site;

import com.english.learning.enums.SlideshowPosition;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.content.slideshow.SlideshowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final SlideshowService slideshowService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getPublishedCategories());
        model.addAttribute("slideshows", slideshowService.getActiveSlideshowsByPosition(SlideshowPosition.HOME));
        return "home";
    }
}
