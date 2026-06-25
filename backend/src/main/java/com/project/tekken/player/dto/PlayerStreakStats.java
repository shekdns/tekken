package com.project.tekken.player.dto;

public record PlayerStreakStats(
        String currentType,
        int currentCount,
        int longestWin,
        int longestLoss
) {
}
