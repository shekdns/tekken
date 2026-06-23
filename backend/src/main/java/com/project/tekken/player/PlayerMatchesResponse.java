package com.project.tekken.player;

import java.time.Instant;
import java.util.List;

public record PlayerMatchesResponse(
        String tekkenId,
        String source,
        Instant fetchedAt,
        List<PlayerMatchSummary> matches
) {
}
