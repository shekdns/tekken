package com.project.tekken.leaderboard;

import java.time.Instant;
import java.util.List;

public record PlayerLeaderboardResponse(
        String source,
        Instant calculatedAt,
        LeaderboardFilters filters,
        List<PlayerLeaderboardItem> items
) {
}
