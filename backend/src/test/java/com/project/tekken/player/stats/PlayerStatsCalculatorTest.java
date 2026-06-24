package com.project.tekken.player.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.tekken.player.dto.PlayerMatchParticipant;
import com.project.tekken.player.dto.PlayerMatchSummary;
import com.project.tekken.player.dto.PlayerStatsFilters;
import com.project.tekken.player.dto.PlayerStatsResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerStatsCalculatorTest {

    @Test
    void calculatesStatsWithBattleTypeCharacterOpponentAndDaysFilters() {
        Instant now = Instant.now();
        List<PlayerMatchSummary> matches = List.of(
                match("1", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", "WIN", "Dragunov", "Bryan"),
                match("2", now.minus(2, ChronoUnit.DAYS), "RANKED_BATTLE", "LOSS", "Dragunov", "Bryan"),
                match("3", now.minus(3, ChronoUnit.DAYS), "QUICK_BATTLE", "WIN", "Dragunov", "Bryan"),
                match("4", now.minus(20, ChronoUnit.DAYS), "RANKED_BATTLE", "WIN", "Dragunov", "Bryan"),
                match("5", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", "WIN", "Kazuya", "Bryan"),
                match("6", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", "WIN", "Dragunov", "Jin"));

        PlayerStatsResponse response = PlayerStatsCalculator.fromMatches(
                "27tB4yhFmfNE",
                "cache",
                now,
                matches,
                100,
                "ranked_battle",
                "dragunov",
                "bryan",
                7);

        assertThat(response.total()).isEqualTo(2);
        assertThat(response.wins()).isEqualTo(1);
        assertThat(response.losses()).isEqualTo(1);
        assertThat(response.winRate()).isEqualTo(50);
        assertThat(response.recent10Record()).isEqualTo("1승 1패");
        assertThat(response.mostPlayedCharacter()).isEqualTo("Dragunov");
        assertThat(response.filters()).isEqualTo(new PlayerStatsFilters(100, "RANKED_BATTLE", "DRAGUNOV", "BRYAN", 7));
        assertThat(response.characterStats())
                .singleElement()
                .satisfies(character -> {
                    assertThat(character.character()).isEqualTo("Dragunov");
                    assertThat(character.games()).isEqualTo(2);
                    assertThat(character.winRate()).isEqualTo(50);
                });
        assertThat(response.battleTypeStats())
                .singleElement()
                .satisfies(battleType -> {
                    assertThat(battleType.battleType()).isEqualTo("RANKED_BATTLE");
                    assertThat(battleType.games()).isEqualTo(2);
                });
        assertThat(response.opponentCharacterStats())
                .singleElement()
                .satisfies(opponent -> {
                    assertThat(opponent.character()).isEqualTo("Bryan");
                    assertThat(opponent.games()).isEqualTo(2);
                    assertThat(opponent.winRate()).isEqualTo(50);
                });
    }

    @Test
    void normalizesLimitAndAllFilters() {
        Instant now = Instant.now();

        PlayerStatsResponse response = PlayerStatsCalculator.fromMatches(
                "27tB4yhFmfNE",
                "ewgf",
                now,
                List.of(match("1", now, "RANKED_BATTLE", "WIN", "Dragunov", "Bryan")),
                0,
                "ALL",
                "",
                null,
                -1);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.filters()).isEqualTo(new PlayerStatsFilters(100, null, null, null, null));
    }

    private static PlayerMatchSummary match(
            String key,
            Instant battleAt,
            String battleType,
            String result,
            String myCharacter,
            String opponentCharacter
    ) {
        return new PlayerMatchSummary(
                key,
                battleAt,
                battleType,
                result,
                "WIN".equals(result) ? 1 : 2,
                null,
                null,
                "3-2",
                new PlayerMatchParticipant(1, "27tB4yhFmfNE", "me", myCharacter, "KR", "God of Destruction", 300000, 3),
                new PlayerMatchParticipant(2, "opponent", "opp", opponentCharacter, "KR", "Tekken God", 250000, 2),
                Map.of());
    }
}
