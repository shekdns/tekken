package com.project.tekken.player;

import java.time.Instant;

public record PlayerProfileSummary(
        String name,
        String rank,
        String mainCharacter,
        String region,
        String platform,
        Integer tekkenProwess,
        String language,
        Instant lastSeen
) {
}
