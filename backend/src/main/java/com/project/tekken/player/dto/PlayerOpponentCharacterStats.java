package com.project.tekken.player.dto;

public record PlayerOpponentCharacterStats(
        String character,
        int games,
        int wins,
        int losses,
        int winRate
) {
}
