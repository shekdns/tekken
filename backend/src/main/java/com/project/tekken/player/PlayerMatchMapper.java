package com.project.tekken.player;

import com.project.tekken.match.MatchEntity;
import java.time.Instant;
import java.util.Map;

final class PlayerMatchMapper {

    private PlayerMatchMapper() {
    }

    static PlayerMatchSummary toSummary(Map<String, Object> battle, String tekkenId) {
        String normalizedTekkenId = normalizeTekkenId(tekkenId);
        PlayerMatchParticipant p1 = participant(battle, 1);
        PlayerMatchParticipant p2 = participant(battle, 2);
        PlayerMatchParticipant my = normalizedTekkenId.equals(normalizeTekkenId(p1.tekkenId())) ? p1 : p2;
        PlayerMatchParticipant opponent = my.side() == 1 ? p2 : p1;
        Integer winnerSide = integer(battle, "winner");

        return new PlayerMatchSummary(
                MatchEntity.externalKey(battle),
                instant(battle, "battle_at", "battleAt"),
                text(battle, "battle_type", "battleType"),
                result(my.side(), winnerSide),
                winnerSide,
                integer(battle, "stage_id", "stageId"),
                integer(battle, "game_version", "gameVersion"),
                roundScore(my.roundsWon(), opponent.roundsWon()),
                my,
                opponent,
                battle);
    }

    private static PlayerMatchParticipant participant(Map<String, Object> battle, int side) {
        String prefix = "p" + side + "_";
        String camelPrefix = "p" + side;

        return new PlayerMatchParticipant(
                side,
                normalizeTekkenId(text(battle, prefix + "tekken_id", camelPrefix + "TekkenId")),
                text(battle, prefix + "name", camelPrefix + "Name"),
                text(battle, prefix + "char", camelPrefix + "Char", prefix + "character", camelPrefix + "Character"),
                text(battle, prefix + "region", camelPrefix + "Region"),
                text(battle, prefix + "dan_rank", camelPrefix + "DanRank"),
                integer(battle, prefix + "tekken_power", camelPrefix + "TekkenPower", prefix + "tekken_prowess"),
                integer(battle, prefix + "rounds_won", camelPrefix + "RoundsWon"));
    }

    private static String result(int mySide, Integer winnerSide) {
        if (winnerSide == null) {
            return "UNKNOWN";
        }
        return winnerSide == mySide ? "WIN" : "LOSS";
    }

    private static String roundScore(Integer myRoundsWon, Integer opponentRoundsWon) {
        if (myRoundsWon == null || opponentRoundsWon == null) {
            return null;
        }
        return myRoundsWon + "-" + opponentRoundsWon;
    }

    private static String normalizeTekkenId(String tekkenId) {
        return tekkenId == null ? null : tekkenId.replace("-", "");
    }

    private static String text(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        return value == null ? null : String.valueOf(value);
    }

    private static Integer integer(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Instant instant(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Object first(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
