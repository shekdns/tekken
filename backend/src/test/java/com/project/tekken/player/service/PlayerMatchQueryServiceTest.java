package com.project.tekken.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.project.tekken.match.MatchEntity;
import com.project.tekken.match.MatchRepository;
import com.project.tekken.player.dto.PlayerMatchesResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerMatchQueryServiceTest {

    private static final String TEKKEN_ID = "27tB4yhFmfNE";

    @Mock
    private MatchRepository matchRepository;

    @Test
    void returnsNullWhenStoredMatchesAreEmpty() {
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(TEKKEN_ID, TEKKEN_ID))
                .thenReturn(List.of());

        assertThat(queryService.findStoredMatches(TEKKEN_ID)).isNull();
    }

    @Test
    void mapsStoredMatchesToDatabaseMatchData() {
        Instant firstFetchedAt = Instant.parse("2026-06-24T01:00:00Z");
        Instant secondFetchedAt = Instant.parse("2026-06-24T02:00:00Z");
        MatchEntity first = storedMatch("stored-1", firstFetchedAt, 1, "Dragunov", "Bryan");
        MatchEntity second = storedMatch("stored-2", secondFetchedAt, 2, "Dragunov", "Jin");
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(TEKKEN_ID, TEKKEN_ID))
                .thenReturn(List.of(first, second));

        PlayerMatchData data = queryService.findStoredMatches(TEKKEN_ID);

        assertThat(data.source()).isEqualTo("database");
        assertThat(data.tekkenId()).isEqualTo(TEKKEN_ID);
        assertThat(data.fetchedAt()).isEqualTo(secondFetchedAt);
        assertThat(data.matches()).hasSize(2);
        assertThat(data.matches().get(0).externalMatchKey()).isEqualTo("stored-1");
        assertThat(data.matches().get(0).my().character()).isEqualTo("Dragunov");
        assertThat(data.matches().get(0).opponent().character()).isEqualTo("Bryan");
        assertThat(data.matches().get(0).result()).isEqualTo("WIN");
    }

    @Test
    void mapsStoredMatchesToPagedDatabaseResponse() {
        Instant fetchedAt = Instant.parse("2026-06-24T02:00:00Z");
        MatchEntity match = storedMatch("stored-page-1", fetchedAt, 1, "Dragunov", "Bryan");
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findLatestFetchedAtByTekkenId(TEKKEN_ID)).thenReturn(Optional.of(fetchedAt));
        when(matchRepository.findPlayerMatches(
                eq(TEKKEN_ID),
                eq("RANKED_BATTLE"),
                eq("DRAGUNOV"),
                eq("BRYAN"),
                eq(Instant.parse("2026-06-01T00:00:00Z")),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match), new OffsetPageRequest(1, 1), 3));

        PlayerMatchesResponse response = queryService.findStoredMatchesResponse(
                TEKKEN_ID,
                1,
                1,
                "RANKED_BATTLE",
                "DRAGUNOV",
                "BRYAN",
                30,
                Instant.parse("2026-06-01T00:00:00Z"));

        assertThat(response.source()).isEqualTo("database");
        assertThat(response.fetchedAt()).isEqualTo(fetchedAt);
        assertThat(response.total()).isEqualTo(3);
        assertThat(response.offset()).isEqualTo(1);
        assertThat(response.limit()).isEqualTo(1);
        assertThat(response.hasMore()).isTrue();
        assertThat(response.nextOffset()).isEqualTo(2);
        assertThat(response.filters().battleType()).isEqualTo("RANKED_BATTLE");
        assertThat(response.filters().character()).isEqualTo("DRAGUNOV");
        assertThat(response.filters().opponentCharacter()).isEqualTo("BRYAN");
        assertThat(response.filters().days()).isEqualTo(30);
        assertThat(response.matches()).singleElement().satisfies(summary -> {
            assertThat(summary.externalMatchKey()).isEqualTo("stored-page-1");
            assertThat(summary.my().character()).isEqualTo("Dragunov");
            assertThat(summary.opponent().character()).isEqualTo("Bryan");
        });
    }

    @Test
    void returnsNullPagedResponseWhenDatabaseHasNoPlayerMatches() {
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findLatestFetchedAtByTekkenId(TEKKEN_ID)).thenReturn(Optional.empty());

        PlayerMatchesResponse response = queryService.findStoredMatchesResponse(
                TEKKEN_ID,
                0,
                12,
                null,
                null,
                null,
                null,
                null);

        assertThat(response).isNull();
    }

    @Test
    void mapsStoredMatchesToStatsData() {
        Instant fetchedAt = Instant.parse("2026-06-24T03:00:00Z");
        MatchEntity first = storedMatch("stats-1", fetchedAt, 1, "Dragunov", "Bryan");
        MatchEntity second = storedMatch("stats-2", fetchedAt.minusSeconds(60), 2, "Dragunov", "Bryan");
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findLatestFetchedAtByTekkenId(TEKKEN_ID)).thenReturn(Optional.of(fetchedAt));
        when(matchRepository.findPlayerMatches(
                eq(TEKKEN_ID),
                eq("RANKED_BATTLE"),
                eq("DRAGUNOV"),
                eq("BRYAN"),
                eq(Instant.parse("2026-06-01T00:00:00Z")),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), new OffsetPageRequest(100, 0), 2));

        PlayerMatchData data = queryService.findStoredStatsData(
                TEKKEN_ID,
                100,
                "RANKED_BATTLE",
                "DRAGUNOV",
                "BRYAN",
                Instant.parse("2026-06-01T00:00:00Z"));

        assertThat(data.source()).isEqualTo("database");
        assertThat(data.fetchedAt()).isEqualTo(fetchedAt);
        assertThat(data.matches()).hasSize(2);
        assertThat(data.matches().get(0).externalMatchKey()).isEqualTo("stats-1");
        assertThat(data.matches().get(1).result()).isEqualTo("LOSS");
    }

    @Test
    void returnsNullStatsDataWhenDatabaseHasNoPlayerMatches() {
        PlayerMatchQueryService queryService = new PlayerMatchQueryService(matchRepository);
        when(matchRepository.findLatestFetchedAtByTekkenId(TEKKEN_ID)).thenReturn(Optional.empty());

        PlayerMatchData data = queryService.findStoredStatsData(
                TEKKEN_ID,
                100,
                null,
                null,
                null,
                null);

        assertThat(data).isNull();
    }

    private static MatchEntity storedMatch(
            String id,
            Instant battleAt,
            int winner,
            String myCharacter,
            String opponentCharacter
    ) {
        MatchEntity entity = new MatchEntity(id, battleAt);
        entity.updateFromBattle(Map.ofEntries(
                Map.entry("id", id),
                Map.entry("battle_at", battleAt.toString()),
                Map.entry("battle_type", "RANKED_BATTLE"),
                Map.entry("winner", winner),
                Map.entry("p1_tekken_id", TEKKEN_ID),
                Map.entry("p1_name", "Me"),
                Map.entry("p1_char", myCharacter),
                Map.entry("p1_rounds_won", winner == 1 ? 3 : 2),
                Map.entry("p2_tekken_id", "opponent-" + id),
                Map.entry("p2_name", "Opponent"),
                Map.entry("p2_char", opponentCharacter),
                Map.entry("p2_rounds_won", winner == 1 ? 2 : 3)), battleAt);
        return entity;
    }
}
