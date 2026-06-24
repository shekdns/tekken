package com.project.tekken.player.dto;

public record PlayerMatchFilters(
        int limit,
        String battleType,
        String character,
        String opponentCharacter,
        Integer days
) {
}
