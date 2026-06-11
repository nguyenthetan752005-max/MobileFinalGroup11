package com.english.learning.controller.site;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class InfoPageController {

    @Value("${app.admin.email:admin@englishlistening.local}")
    private String adminEmail;

    @GetMapping("/expressions")
    public String expressions(Model model) {
        model.addAttribute("pageTitle", "English Expressions");
        model.addAttribute("pageHeading", "Useful English Expressions");
        model.addAttribute("pageLead", "Common expressions for daily conversations, listening practice, and natural communication.");
        model.addAttribute("sections", List.of(
                section("Everyday conversation", List.of(
                        "How's it going?",
                        "That makes sense.",
                        "I'm just looking around.",
                        "Could you say that again?"
                )),
                section("Listening practice tips", List.of(
                        "Listen for context before translating word by word.",
                        "Repeat short expressions aloud to improve retention.",
                        "Focus on collocations and fixed phrases."
                ))
        ));
        return "info-page";
    }

    @GetMapping("/pronunciation")
    public String pronunciation(Model model) {
        model.addAttribute("pageTitle", "English Pronunciation");
        model.addAttribute("pageHeading", "English Pronunciation Guide");
        model.addAttribute("pageLead", "Quick guidance to improve clarity when listening and speaking English.");
        model.addAttribute("sections", List.of(
                section("Core habits", List.of(
                        "Shadow short audio clips and match rhythm first.",
                        "Practice word stress before individual sounds.",
                        "Record yourself and compare with the original."
                )),
                section("What to focus on", List.of(
                        "Final consonants",
                        "Connected speech",
                        "Sentence stress and intonation"
                ))
        ));
        return "info-page";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact");
        model.addAttribute("pageHeading", "Contact");
        model.addAttribute("pageLead", "If you find a bug, broken lesson, or account issue, use the channels below.");
        model.addAttribute("sections", List.of(
                section("Support", List.of(
                        "Email: " + adminEmail,
                        "Admin dashboard is intended for internal management only.",
                        "Include your username and the page URL when reporting a problem."
                ))
        ));
        return "info-page";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "Terms & Rules");
        model.addAttribute("pageHeading", "Terms & Rules");
        model.addAttribute("pageLead", "Basic usage rules for the learning platform.");
        model.addAttribute("sections", List.of(
                section("Account usage", List.of(
                        "Do not share accounts or abuse ranking features.",
                        "Do not upload harmful or illegal content.",
                        "Administrators may moderate comments and content for system safety."
                )),
                section("Learning content", List.of(
                        "Content is provided for educational use.",
                        "Availability of lessons, slideshows, and exercises may change over time."
                ))
        ));
        return "info-page";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Policy");
        model.addAttribute("pageHeading", "Privacy Policy");
        model.addAttribute("pageLead", "Summary of the main data used by the platform.");
        model.addAttribute("sections", List.of(
                section("Data we store", List.of(
                        "Account information such as username, email, avatar, and login provider.",
                        "Learning progress, speaking results, and comments.",
                        "Security-related records such as password reset tokens."
                )),
                section("How data is used", List.of(
                        "To authenticate users and protect accounts.",
                        "To track learning progress and show personalized results.",
                        "To moderate content and maintain platform stability."
                ))
        ));
        return "info-page";
    }

    private PageSection section(String title, List<String> items) {
        return new PageSection(title, items);
    }

    public record PageSection(String title, List<String> items) {}
}
