package com.project.tekken.player.dto;

import java.time.Instant;
import java.util.List;

public record PlayerMatchesResponse(
        String tekkenId,
        String source,
        Instant fetchedAt,
        int total,
        int offset,
        int limit,
        boolean hasMore,
        Integer nextOffset,
        PlayerMatchFilters filters,
        List<PlayerMatchSummary> matches
) {
}
