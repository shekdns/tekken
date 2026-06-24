package com.project.tekken.player.dto;

public record PlayerStatsFilters(
        int limit,
        String battleType,
        String character,
        String opponentCharacter,
        Integer days
) {
}
