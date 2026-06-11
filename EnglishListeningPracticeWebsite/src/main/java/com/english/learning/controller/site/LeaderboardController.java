package com.english.learning.controller.site;

import com.english.learning.entity.User;
import com.english.learning.service.leaderboard.LeaderboardService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Design Logic: Leaderboard Controller (MVC - Thin Controller).
 *
 * - Only handles HTTP routing and session checking.
 * - Delegates ALL business logic (data formatting, aggregation) to LeaderboardService.
 * - Does NOT access Repositories directly (MVC compliance).
 */
@Controller
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/top-users")
    public String showTopUsersPage(Model model, HttpSession session) {
        // 1. Retrieve pre-formatted leaderboard data from Service
        model.addAttribute("top7Days", leaderboardService.getTopUsers7Days());
        model.addAttribute("top30Days", leaderboardService.getTopUsers30Days());

        // 2. Current user's active time (from session, no direct repo access)
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            int t7 = loggedInUser.getActiveTime7d() != null ? loggedInUser.getActiveTime7d() : 0;
            int t30 = loggedInUser.getActiveTime30d() != null ? loggedInUser.getActiveTime30d() : 0;
            model.addAttribute("currentUserTime7d", TimeFormatUtil.formatActiveTime(t7));
            model.addAttribute("currentUserTime30d", TimeFormatUtil.formatActiveTime(t30));
        }

        return "leaderboard/top-users";
    }
}
