package com.project.tekken.player.dto;

public record PlayerBattleTypeStats(
        String battleType,
        int games,
        int wins,
        int losses,
        int winRate
) {
}
