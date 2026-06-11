package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileLeaderboardResponse;
import com.english.learning.mapper.mobile.MobileLeaderboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller: Mobile Leaderboard API.
 * Provides standalone leaderboard endpoint for Android.
 */
@RestController
@RequestMapping("/api/mobile/leaderboard")
@RequiredArgsConstructor
public class MobileLeaderboardController {

    private final MobileLeaderboardMapper mapper;

    /**
     * GET /api/mobile/leaderboard
     * Returns weekly and monthly leaderboard entries.
     */
    @GetMapping
    public ResponseEntity<MobileLeaderboardResponse> getLeaderboard() {
        MobileLeaderboardResponse leaderboard = mapper.buildLeaderboard(getAuthenticatedUserId());
        return ResponseEntity.ok(leaderboard);
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
