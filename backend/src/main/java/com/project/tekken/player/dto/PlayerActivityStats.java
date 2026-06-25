package com.project.tekken.player.dto;

import java.time.Instant;

public record PlayerActivityStats(
        Instant firstBattleAt,
        Instant latestBattleAt,
        int activeDays
) {
}
