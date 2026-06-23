package com.project.tekken.player;

import java.time.Instant;
import java.util.Map;

public record PlayerMatchSummary(
        String externalMatchKey,
        Instant battleAt,
        String battleType,
        String result,
        Integer winnerSide,
        Integer stageId,
        Integer gameVersion,
        String roundScore,
        PlayerMatchParticipant my,
        PlayerMatchParticipant opponent,
        Map<String, Object> raw
) {
}
