package com.project.tekken.player.dto;

public record PlayerMatchParticipant(
        int side,
        String tekkenId,
        String name,
        String character,
        String region,
        String rank,
        Integer tekkenPower,
        Integer roundsWon
) {
}
