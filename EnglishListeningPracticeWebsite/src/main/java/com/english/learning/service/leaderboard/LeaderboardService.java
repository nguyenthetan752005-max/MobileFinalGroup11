package com.english.learning.service.leaderboard;

import java.util.List;
import java.util.Map;

/**
 * Design Logic: Leaderboard Service (Service Layer - SOLID SRP).
 *
 * - Sole responsibility: Manages leaderboard data (cache refresh + retrieval).
 * - The Cron Job refreshes cached 7D/30D values in the User table every hour.
 * - getTopUsers returns pre-formatted row data ready for the View layer,
 *   keeping the Controller thin (MVC compliance).
 */
public interface LeaderboardService {
    void refreshLeaderboardCaches();
    List<Map<String, Object>> getTopUsers7Days();
    List<Map<String, Object>> getTopUsers30Days();
}

