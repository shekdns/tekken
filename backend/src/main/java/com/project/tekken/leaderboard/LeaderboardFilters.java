package com.project.tekken.leaderboard;

public record LeaderboardFilters(
        String sort,
        String character,
        String region,
        String platform,
        int limit
) {
}
