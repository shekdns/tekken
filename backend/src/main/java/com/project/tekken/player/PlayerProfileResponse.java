package com.project.tekken.player;

import java.time.Instant;
import java.util.Map;

public record PlayerProfileResponse(
        String tekkenId,
        String source,
        Instant fetchedAt,
        PlayerProfileSummary summary,
        Map<String, Object> profile
) {
}
