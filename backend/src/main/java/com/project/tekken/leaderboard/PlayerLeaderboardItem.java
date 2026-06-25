package com.project.tekken.leaderboard;

import java.time.Instant;

public record PlayerLeaderboardItem(
        int rank,
        String tekkenId,
        String name,
        String mainCharacter,
        String danRank,
        Integer tekkenProwess,
        String region,
        String platform,
        long searchCount,
        Instant lastUpdatedAt
) {
}
