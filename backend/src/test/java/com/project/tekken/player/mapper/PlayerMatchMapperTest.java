package com.project.tekken.player.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.tekken.match.MatchEntity;
import com.project.tekken.player.dto.PlayerMatchParticipant;
import com.project.tekken.player.dto.PlayerMatchSummary;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerMatchMapperTest {

    @Test
    void mapsSnakeCaseBattleToMyOpponentAndWinResult() {
        Map<String, Object> battle = new LinkedHashMap<>();
        battle.put("battle_at", "2026-06-23T10:15:30Z");
        battle.put("battle_type", "RANKED_BATTLE");
        battle.put("winner", 1);
        battle.put("stage_id", "12");
        battle.put("game_version", 200);
        battle.put("p1_tekken_id", "27tB-4yhF-mfNE");
        battle.put("p1_name", "Me");
        battle.put("p1_char", "Dragunov");
        battle.put("p1_region", "KR");
        battle.put("p1_dan_rank", "God of Destruction");
        battle.put("p1_tekken_power", "369393");
        battle.put("p1_rounds_won", 3);
        battle.put("p2_tekken_id", "opp-1234");
        battle.put("p2_name", "Opponent");
        battle.put("p2_char", "Bryan");
        battle.put("p2_region", "JP");
        battle.put("p2_dan_rank", "Tekken God");
        battle.put("p2_tekken_power", 280000);
        battle.put("p2_rounds_won", 2);

        PlayerMatchSummary summary = PlayerMatchMapper.toSummary(battle, "27tB4yhFmfNE");

        assertThat(summary.battleAt()).isEqualTo(Instant.parse("2026-06-23T10:15:30Z"));
        assertThat(summary.battleType()).isEqualTo("RANKED_BATTLE");
        assertThat(summary.result()).isEqualTo("WIN");
        assertThat(summary.winnerSide()).isEqualTo(1);
        assertThat(summary.stageId()).isEqualTo(12);
        assertThat(summary.gameVersion()).isEqualTo(200);
        assertThat(summary.roundScore()).isEqualTo("3-2");
        assertThat(summary.my()).isEqualTo(new PlayerMatchParticipant(
                1,
                "27tB4yhFmfNE",
                "Me",
                "Dragunov",
                "KR",
                "God of Destruction",
                369393,
                3));
        assertThat(summary.opponent()).isEqualTo(new PlayerMatchParticipant(
                2,
                "opp1234",
                "Opponent",
                "Bryan",
                "JP",
                "Tekken God",
                280000,
                2));
        assertThat(summary.raw()).isSameAs(battle);
        assertThat(summary.externalMatchKey()).contains("2026-06-23T10:15:30Z");
    }

    @Test
    void mapsCamelCaseBattleWhenTargetIsPlayerTwo() {
        Map<String, Object> battle = Map.ofEntries(
                Map.entry("battleAt", "2026-06-23T11:00:00Z"),
                Map.entry("battleType", "QUICK_BATTLE"),
                Map.entry("winner", "1"),
                Map.entry("p1TekkenId", "winner-player"),
                Map.entry("p1Name", "Winner"),
                Map.entry("p1Character", "Jin"),
                Map.entry("p1DanRank", "Tekken King"),
                Map.entry("p1RoundsWon", "3"),
                Map.entry("p2TekkenId", "27tB-4yhF-mfNE"),
                Map.entry("p2Name", "Me"),
                Map.entry("p2Character", "Kazuya"),
                Map.entry("p2DanRank", "Bushin"),
                Map.entry("p2RoundsWon", "1"));

        PlayerMatchSummary summary = PlayerMatchMapper.toSummary(battle, "27tB-4yhF-mfNE");

        assertThat(summary.result()).isEqualTo("LOSS");
        assertThat(summary.my().side()).isEqualTo(2);
        assertThat(summary.my().tekkenId()).isEqualTo("27tB4yhFmfNE");
        assertThat(summary.my().character()).isEqualTo("Kazuya");
        assertThat(summary.opponent().side()).isEqualTo(1);
        assertThat(summary.opponent().character()).isEqualTo("Jin");
        assertThat(summary.roundScore()).isEqualTo("1-3");
    }

    @Test
    void mapsUnknownResultAndInvalidValuesSafely() {
        Map<String, Object> battle = Map.of(
                "battle_at", "not-a-date",
                "stage_id", "unknown",
                "p1_tekken_id", "target",
                "p1_rounds_won", 2,
                "p2_tekken_id", "opponent");

        PlayerMatchSummary summary = PlayerMatchMapper.toSummary(battle, "target");

        assertThat(summary.battleAt()).isNull();
        assertThat(summary.stageId()).isNull();
        assertThat(summary.result()).isEqualTo("UNKNOWN");
        assertThat(summary.roundScore()).isNull();
        assertThat(summary.my().tekkenId()).isEqualTo("target");
        assertThat(summary.opponent().tekkenId()).isEqualTo("opponent");
    }

    @Test
    void mapsStoredMatchEntityToSummary() {
        Instant now = Instant.parse("2026-06-23T10:15:30Z");
        MatchEntity entity = new MatchEntity("stored-1", now);
        entity.updateFromBattle(Map.ofEntries(
                Map.entry("id", "stored-1"),
                Map.entry("battle_at", now.toString()),
                Map.entry("battle_type", "RANKED_BATTLE"),
                Map.entry("winner", 2),
                Map.entry("p1_tekken_id", "opponent"),
                Map.entry("p1_name", "Opponent"),
                Map.entry("p1_char", "Bryan"),
                Map.entry("p1_rounds_won", 2),
                Map.entry("p2_tekken_id", "27tB-4yhF-mfNE"),
                Map.entry("p2_name", "Me"),
                Map.entry("p2_char", "Dragunov"),
                Map.entry("p2_rounds_won", 3)), now);

        PlayerMatchSummary summary = PlayerMatchMapper.toSummary(entity, "27tB-4yhF-mfNE");

        assertThat(summary.externalMatchKey()).isEqualTo("stored-1");
        assertThat(summary.result()).isEqualTo("WIN");
        assertThat(summary.my().side()).isEqualTo(2);
        assertThat(summary.my().character()).isEqualTo("Dragunov");
        assertThat(summary.opponent().character()).isEqualTo("Bryan");
        assertThat(summary.roundScore()).isEqualTo("3-2");
        assertThat(summary.raw()).containsEntry("id", "stored-1");
    }
}
