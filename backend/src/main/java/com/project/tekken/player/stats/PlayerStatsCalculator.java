package com.project.tekken.player.stats;

import com.project.tekken.player.dto.PlayerBattleTypeStats;
import com.project.tekken.player.dto.PlayerActivityStats;
import com.project.tekken.player.dto.PlayerCharacterStats;
import com.project.tekken.player.dto.PlayerMatchSummary;
import com.project.tekken.player.dto.PlayerOpponentCharacterStats;
import com.project.tekken.player.dto.PlayerStreakStats;
import com.project.tekken.player.dto.PlayerStatsFilters;
import com.project.tekken.player.dto.PlayerStatsResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public final class PlayerStatsCalculator {

    private PlayerStatsCalculator() {
    }

    public static PlayerStatsResponse fromMatches(
            String tekkenId,
            String source,
            Instant fetchedAt,
            List<PlayerMatchSummary> matches,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days
    ) {
        int normalizedLimit = normalizeLimit(limit);
        String normalizedBattleType = normalizeBattleType(battleType);
        String normalizedCharacter = normalizeTextFilter(character);
        String normalizedOpponentCharacter = normalizeTextFilter(opponentCharacter);
        Integer normalizedDays = normalizeDays(days);
        Instant fromBattleAt = normalizedDays == null ? null : Instant.now().minus(normalizedDays, ChronoUnit.DAYS);
        List<PlayerMatchSummary> filteredMatches = matches.stream()
                .filter(match -> normalizedBattleType == null || normalizedBattleType.equals(match.battleType()))
                .filter(match -> normalizedCharacter == null || normalizedCharacter.equals(normalizeTextFilter(match.my() == null ? null : match.my().character())))
                .filter(match -> normalizedOpponentCharacter == null || normalizedOpponentCharacter.equals(normalizeTextFilter(match.opponent() == null ? null : match.opponent().character())))
                .filter(match -> fromBattleAt == null || (match.battleAt() != null && !match.battleAt().isBefore(fromBattleAt)))
                .limit(normalizedLimit)
                .toList();

        int total = filteredMatches.size();
        int wins = (int) filteredMatches.stream().filter(match -> "WIN".equals(match.result())).count();
        int losses = (int) filteredMatches.stream().filter(match -> "LOSS".equals(match.result())).count();
        int recent10Wins = (int) filteredMatches.stream().limit(10).filter(match -> "WIN".equals(match.result())).count();
        int recent10Losses = Math.max(Math.min(total, 10) - recent10Wins, 0);
        List<PlayerCharacterStats> characterStats = characterStats(filteredMatches);

        return new PlayerStatsResponse(
                tekkenId,
                source,
                fetchedAt,
                total,
                wins,
                losses,
                percentage(wins, total),
                recent10Wins,
                recent10Losses,
                recent10Wins + "승 " + recent10Losses + "패",
                characterStats.isEmpty() ? "-" : characterStats.get(0).character(),
                characterStats,
                new PlayerStatsFilters(
                        normalizedLimit,
                        normalizedBattleType,
                        normalizedCharacter,
                        normalizedOpponentCharacter,
                        normalizedDays),
                battleTypeStats(filteredMatches),
                opponentCharacterStats(filteredMatches),
                streakStats(filteredMatches),
                activityStats(filteredMatches));
    }

    private static PlayerStreakStats streakStats(List<PlayerMatchSummary> matches) {
        String currentType = null;
        int currentCount = 0;
        boolean currentStreakOpen = true;
        int longestWin = 0;
        int longestLoss = 0;
        int currentWin = 0;
        int currentLoss = 0;

        for (PlayerMatchSummary match : matches) {
            String result = match.result();
            if (!"WIN".equals(result) && !"LOSS".equals(result)) {
                currentStreakOpen = false;
                currentWin = 0;
                currentLoss = 0;
                continue;
            }

            if (currentStreakOpen && currentType == null) {
                currentType = result;
                currentCount = 1;
            } else if (currentStreakOpen && currentType.equals(result)) {
                currentCount++;
            } else {
                currentStreakOpen = false;
            }

            if ("WIN".equals(result)) {
                currentWin++;
                currentLoss = 0;
                longestWin = Math.max(longestWin, currentWin);
            } else {
                currentLoss++;
                currentWin = 0;
                longestLoss = Math.max(longestLoss, currentLoss);
            }
        }

        return new PlayerStreakStats(currentType == null ? "-" : currentType, currentCount, longestWin, longestLoss);
    }

    private static PlayerActivityStats activityStats(List<PlayerMatchSummary> matches) {
        Instant firstBattleAt = matches.stream()
                .map(PlayerMatchSummary::battleAt)
                .filter(value -> value != null)
                .min(Instant::compareTo)
                .orElse(null);
        Instant latestBattleAt = matches.stream()
                .map(PlayerMatchSummary::battleAt)
                .filter(value -> value != null)
                .max(Instant::compareTo)
                .orElse(null);
        int activeDays = (int) matches.stream()
                .map(PlayerMatchSummary::battleAt)
                .filter(value -> value != null)
                .map(value -> LocalDate.ofInstant(value, ZoneOffset.UTC))
                .distinct()
                .count();

        return new PlayerActivityStats(firstBattleAt, latestBattleAt, activeDays);
    }

    private static List<PlayerCharacterStats> characterStats(List<PlayerMatchSummary> matches) {
        Map<String, MutableCharacterStats> stats = new LinkedHashMap<>();
        for (PlayerMatchSummary match : matches) {
            String character = match.my() == null || match.my().character() == null ? "-" : match.my().character();
            MutableCharacterStats current = stats.computeIfAbsent(character, MutableCharacterStats::new);
            current.games++;
            if ("WIN".equals(match.result())) {
                current.wins++;
            } else if ("LOSS".equals(match.result())) {
                current.losses++;
            }
        }

        return stats.values().stream()
                .map(MutableCharacterStats::toResponse)
                .sorted(Comparator
                        .comparingInt(PlayerCharacterStats::games).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerCharacterStats::winRate).reversed()))
                .limit(5)
                .toList();
    }

    private static List<PlayerBattleTypeStats> battleTypeStats(List<PlayerMatchSummary> matches) {
        Map<String, MutableCharacterStats> stats = new LinkedHashMap<>();
        for (PlayerMatchSummary match : matches) {
            String battleType = match.battleType() == null ? "-" : match.battleType();
            MutableCharacterStats current = stats.computeIfAbsent(battleType, MutableCharacterStats::new);
            current.games++;
            if ("WIN".equals(match.result())) {
                current.wins++;
            } else if ("LOSS".equals(match.result())) {
                current.losses++;
            }
        }

        return stats.values().stream()
                .map(value -> new PlayerBattleTypeStats(
                        value.character,
                        value.games,
                        value.wins,
                        value.losses,
                        percentage(value.wins, value.games)))
                .sorted(Comparator
                        .comparingInt(PlayerBattleTypeStats::games).reversed()
                        .thenComparing(PlayerBattleTypeStats::battleType))
                .toList();
    }

    private static List<PlayerOpponentCharacterStats> opponentCharacterStats(List<PlayerMatchSummary> matches) {
        Map<String, MutableCharacterStats> stats = new LinkedHashMap<>();
        for (PlayerMatchSummary match : matches) {
            String character = match.opponent() == null || match.opponent().character() == null ? "-" : match.opponent().character();
            MutableCharacterStats current = stats.computeIfAbsent(character, MutableCharacterStats::new);
            current.games++;
            if ("WIN".equals(match.result())) {
                current.wins++;
            } else if ("LOSS".equals(match.result())) {
                current.losses++;
            }
        }

        return stats.values().stream()
                .map(value -> new PlayerOpponentCharacterStats(
                        value.character,
                        value.games,
                        value.wins,
                        value.losses,
                        percentage(value.wins, value.games)))
                .sorted(Comparator
                        .comparingInt(PlayerOpponentCharacterStats::games).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerOpponentCharacterStats::winRate).reversed()))
                .limit(10)
                .toList();
    }

    private static int percentage(int value, int total) {
        return total == 0 ? 0 : Math.round((value * 100f) / total);
    }

    private static int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 100;
        }
        return Math.min(limit, 500);
    }

    private static String normalizeBattleType(String battleType) {
        if (battleType == null || battleType.isBlank() || "ALL".equalsIgnoreCase(battleType)) {
            return null;
        }
        return battleType.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeTextFilter(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static Integer normalizeDays(Integer days) {
        if (days == null || days <= 0) {
            return null;
        }
        return Math.min(days, 365);
    }

    private static final class MutableCharacterStats {
        private final String character;
        private int games;
        private int wins;
        private int losses;

        private MutableCharacterStats(String character) {
            this.character = character;
        }

        private PlayerCharacterStats toResponse() {
            return new PlayerCharacterStats(
                    character,
                    games,
                    wins,
                    losses,
                    percentage(wins, games));
        }
    }
}
