package com.project.tekken.player.service;

import com.project.tekken.player.dto.PlayerMatchSummary;
import java.time.Instant;
import java.util.List;

record PlayerMatchData(
        String tekkenId,
        String source,
        Instant fetchedAt,
        List<PlayerMatchSummary> matches
) {
    boolean isFresh(Instant now, java.time.Duration ttl) {
        return fetchedAt != null && fetchedAt.plus(ttl).isAfter(now);
    }
}
