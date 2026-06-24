package com.project.tekken.player.dto;

import java.time.Instant;
import java.util.List;

public record PlayerStatsResponse(
        String tekkenId,
        String source,
        Instant fetchedAt,
        int total,
        int wins,
        int losses,
        int winRate,
        int recent10Wins,
        int recent10Losses,
        String recent10Record,
        String mostPlayedCharacter,
        List<PlayerCharacterStats> characterStats,
        PlayerStatsFilters filters,
        List<PlayerBattleTypeStats> battleTypeStats,
        List<PlayerOpponentCharacterStats> opponentCharacterStats
) {
}
