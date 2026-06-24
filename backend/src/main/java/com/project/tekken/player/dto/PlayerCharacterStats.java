package com.project.tekken.player.dto;

public record PlayerCharacterStats(
        String character,
        int games,
        int wins,
        int losses,
        int winRate
) {
}
