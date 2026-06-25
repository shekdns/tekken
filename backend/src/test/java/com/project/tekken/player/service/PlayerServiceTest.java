package com.project.tekken.player.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import com.project.tekken.datasource.ewgf.EwgfApiClient;
import com.project.tekken.match.MatchEntity;
import com.project.tekken.match.MatchRepository;
import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import com.project.tekken.player.dto.PlayerMatchFilters;
import com.project.tekken.player.dto.PlayerMatchesResponse;
import com.project.tekken.player.dto.PlayerProfileResponse;
import com.project.tekken.player.dto.PlayerStatsFilters;
import com.project.tekken.player.dto.PlayerStatsResponse;
import com.project.tekken.player.exception.PlayerApiException;
import com.project.tekken.search.PlayerSearchHistoryEntity;
import com.project.tekken.search.PlayerSearchHistoryRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    private static final String TEKKEN_ID = "27tB-4yhF-mfNE";
    private static final String NORMALIZED_TEKKEN_ID = "27tB4yhFmfNE";

    @Mock
    private EwgfApiClient ewgfApiClient;

    @Mock
    private ApiCacheRepository apiCacheRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerSearchHistoryRepository searchHistoryRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        PlayerMatchQueryService matchQueryService = new PlayerMatchQueryService(matchRepository);
        PlayerMatchSyncService matchSyncService = new PlayerMatchSyncService(
                ewgfApiClient,
                objectMapper,
                apiCacheRepository,
                matchRepository,
                matchQueryService);
        playerService = new PlayerService(
                ewgfApiClient,
                objectMapper,
                apiCacheRepository,
                playerRepository,
                searchHistoryRepository,
                matchSyncService,
                matchQueryService);
    }

    @Test
    void returnsProfileFromFreshCacheWithoutCallingEwgf() {
        Instant cachedAt = Instant.now().minus(5, ChronoUnit.MINUTES);
        ApiCacheEntity cache = new ApiCacheEntity(
                "ewgf:profile:" + NORMALIZED_TEKKEN_ID,
                "ewgf",
                Map.of("data", profile("Cached Player", "Dragunov", "God of Destruction")),
                Instant.now().plus(10, ChronoUnit.MINUTES),
                cachedAt);
        when(apiCacheRepository.findById("ewgf:profile:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(cache));

        PlayerProfileResponse response = playerService.getProfile(TEKKEN_ID);

        assertThat(response.tekkenId()).isEqualTo(NORMALIZED_TEKKEN_ID);
        assertThat(response.source()).isEqualTo("cache");
        assertThat(response.fetchedAt()).isEqualTo(cache.getUpdatedAt());
        assertThat(response.summary().name()).isEqualTo("Cached Player");
        assertThat(response.summary().mainCharacter()).isEqualTo("Dragunov");
        assertThat(response.summary().rank()).isEqualTo("God of Destruction");
        verify(searchHistoryRepository).save(any(PlayerSearchHistoryEntity.class));
        verifyNoInteractions(ewgfApiClient);
        verify(playerRepository, never()).save(any());
        verify(apiCacheRepository, never()).save(any());
    }

    @Test
    void fetchesProfileFromEwgfAndPersistsPlayerAndCacheWhenCacheMisses() {
        when(apiCacheRepository.findById("ewgf:profile:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(playerRepository.findByTekkenId(NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(ewgfApiClient.getProfile(TEKKEN_ID)).thenReturn(ResponseEntity.ok("""
                {
                  "data": {
                    "nickname": "Live Player",
                    "main_character": { "Kazuya": "Bushin" },
                    "tekken_prowess": 250000
                  }
                }
                """));

        PlayerProfileResponse response = playerService.getProfile(TEKKEN_ID);

        assertThat(response.source()).isEqualTo("ewgf");
        assertThat(response.summary().name()).isEqualTo("Live Player");
        assertThat(response.summary().mainCharacter()).isEqualTo("Kazuya");
        assertThat(response.summary().rank()).isEqualTo("Bushin");
        assertThat(response.summary().tekkenProwess()).isEqualTo(250000);
        verify(ewgfApiClient).getProfile(TEKKEN_ID);
        verify(playerRepository).save(any(PlayerEntity.class));
        verify(apiCacheRepository).save(any(ApiCacheEntity.class));
    }

    @Test
    void forceRefreshesProfileFromEwgfEvenWhenFreshCacheExists() {
        ApiCacheEntity cache = new ApiCacheEntity(
                "ewgf:profile:" + NORMALIZED_TEKKEN_ID,
                "ewgf",
                Map.of("data", profile("Cached Player", "Dragunov", "God of Destruction")),
                Instant.now().plus(10, ChronoUnit.MINUTES),
                Instant.now().minus(1, ChronoUnit.MINUTES));
        when(apiCacheRepository.findById("ewgf:profile:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(cache));
        when(playerRepository.findByTekkenId(NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(ewgfApiClient.getProfile(TEKKEN_ID)).thenReturn(ResponseEntity.ok("""
                {
                  "data": {
                    "nickname": "Refreshed Player",
                    "main_character": { "Jin": "Tekken God" },
                    "tekken_prowess": 300000
                  }
                }
                """));

        PlayerProfileResponse response = playerService.getProfile(TEKKEN_ID, true);

        assertThat(response.source()).isEqualTo("ewgf");
        assertThat(response.summary().name()).isEqualTo("Refreshed Player");
        assertThat(response.summary().mainCharacter()).isEqualTo("Jin");
        assertThat(response.summary().rank()).isEqualTo("Tekken God");
        verify(ewgfApiClient).getProfile(TEKKEN_ID);
        verify(playerRepository).save(any(PlayerEntity.class));
        verify(apiCacheRepository).save(any(ApiCacheEntity.class));
    }

    @Test
    void filtersAndPagesMatchesFromCache() {
        Instant now = Instant.now();
        ApiCacheEntity cache = new ApiCacheEntity(
                "ewgf:matches:" + NORMALIZED_TEKKEN_ID,
                "ewgf",
                Map.of("data", List.of(
                        battle("1", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Dragunov", "Bryan"),
                        battle("2", now.minus(2, ChronoUnit.DAYS), "RANKED_BATTLE", 2, "Dragunov", "Bryan"),
                        battle("3", now.minus(3, ChronoUnit.DAYS), "QUICK_BATTLE", 1, "Dragunov", "Bryan"),
                        battle("4", now.minus(4, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Kazuya", "Bryan"),
                        battle("5", now.minus(60, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Dragunov", "Bryan"))),
                Instant.now().plus(10, ChronoUnit.MINUTES),
                now.minus(1, ChronoUnit.MINUTES));
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(cache));

        PlayerMatchesResponse response = playerService.getMatches(
                TEKKEN_ID,
                1,
                1,
                "ranked_battle",
                "dragunov",
                "bryan",
                30);

        assertThat(response.source()).isEqualTo("cache");
        assertThat(response.total()).isEqualTo(2);
        assertThat(response.offset()).isEqualTo(1);
        assertThat(response.limit()).isEqualTo(1);
        assertThat(response.hasMore()).isFalse();
        assertThat(response.nextOffset()).isNull();
        assertThat(response.filters()).isEqualTo(new PlayerMatchFilters(1, "RANKED_BATTLE", "DRAGUNOV", "BRYAN", 30));
        assertThat(response.matches())
                .singleElement()
                .satisfies(match -> {
                    assertThat(match.externalMatchKey()).isEqualTo("2");
                    assertThat(match.result()).isEqualTo("LOSS");
                    assertThat(match.roundScore()).isEqualTo("2-3");
                });
        verifyNoInteractions(ewgfApiClient);
        verify(matchRepository, never()).save(any());
    }

    @Test
    void forceRefreshesMatchesFromEwgfEvenWhenFreshCacheExists() {
        Instant now = Instant.now();
        ApiCacheEntity cache = new ApiCacheEntity(
                "ewgf:matches:" + NORMALIZED_TEKKEN_ID,
                "ewgf",
                Map.of("data", List.of(battle("cached-1", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Dragunov", "Bryan"))),
                Instant.now().plus(10, ChronoUnit.MINUTES),
                now.minus(1, ChronoUnit.MINUTES));
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(cache));
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                NORMALIZED_TEKKEN_ID,
                NORMALIZED_TEKKEN_ID)).thenReturn(List.of());
        when(matchRepository.findByExternalMatchKey(anyString())).thenReturn(Optional.empty());
        when(ewgfApiClient.getBattles(TEKKEN_ID)).thenReturn(ResponseEntity.ok("""
                {
                  "data": [
                    {
                      "id": "force-1",
                      "battle_at": "%s",
                      "battle_type": "RANKED_BATTLE",
                      "winner": 1,
                      "p1_tekken_id": "%s",
                      "p1_name": "Me",
                      "p1_char": "Kazuya",
                      "p1_rounds_won": 3,
                      "p2_tekken_id": "opponent",
                      "p2_name": "Opponent",
                      "p2_char": "Jin",
                      "p2_rounds_won": 2
                    }
                  ]
                }
                """.formatted(now.toString(), TEKKEN_ID)));

        PlayerMatchesResponse response = playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null, true);

        assertThat(response.source()).isEqualTo("ewgf");
        assertThat(response.matches()).singleElement().satisfies(match -> {
            assertThat(match.externalMatchKey()).isEqualTo("force-1");
            assertThat(match.my().character()).isEqualTo("Kazuya");
        });
        verify(ewgfApiClient).getBattles(TEKKEN_ID);
        verify(matchRepository).save(any(MatchEntity.class));
        verify(apiCacheRepository).save(any(ApiCacheEntity.class));
    }

    @Test
    void fetchesMatchesFromEwgfAndPersistsValidBattlesWhenCacheMisses() {
        Instant now = Instant.now();
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                NORMALIZED_TEKKEN_ID,
                NORMALIZED_TEKKEN_ID)).thenReturn(List.of());
        when(matchRepository.findByExternalMatchKey(anyString())).thenReturn(Optional.empty());
        when(ewgfApiClient.getBattles(TEKKEN_ID)).thenReturn(ResponseEntity.ok("""
                {
                  "data": [
                    {
                      "id": "live-1",
                      "battle_at": "%s",
                      "battle_type": "RANKED_BATTLE",
                      "winner": 1,
                      "p1_tekken_id": "%s",
                      "p1_name": "Me",
                      "p1_char": "Dragunov",
                      "p1_rounds_won": 3,
                      "p2_tekken_id": "opponent",
                      "p2_name": "Opponent",
                      "p2_char": "Bryan",
                      "p2_rounds_won": 2
                    }
                  ]
                }
                """.formatted(now.toString(), TEKKEN_ID)));

        PlayerMatchesResponse response = playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null);

        assertThat(response.source()).isEqualTo("ewgf");
        assertThat(response.total()).isEqualTo(1);
        assertThat(response.matches()).singleElement().satisfies(match -> {
            assertThat(match.externalMatchKey()).isEqualTo("live-1");
            assertThat(match.my().character()).isEqualTo("Dragunov");
            assertThat(match.opponent().character()).isEqualTo("Bryan");
        });
        verify(ewgfApiClient).getBattles(TEKKEN_ID);
        verify(matchRepository).save(any(MatchEntity.class));
        verify(apiCacheRepository).save(any(ApiCacheEntity.class));
    }

    @Test
    void returnsFreshStoredDatabaseMatchesBeforeCallingEwgfWhenCacheMisses() {
        Instant now = Instant.now();
        MatchEntity storedMatch = storedMatch("fresh-stored-1", now, "RANKED_BATTLE", 1, "Dragunov", "Bryan");
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                NORMALIZED_TEKKEN_ID,
                NORMALIZED_TEKKEN_ID)).thenReturn(List.of(storedMatch));

        PlayerMatchesResponse response = playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null);

        assertThat(response.source()).isEqualTo("database");
        assertThat(response.total()).isEqualTo(1);
        assertThat(response.matches()).singleElement().satisfies(match -> {
            assertThat(match.externalMatchKey()).isEqualTo("fresh-stored-1");
            assertThat(match.my().character()).isEqualTo("Dragunov");
        });
        verifyNoInteractions(ewgfApiClient);
        verify(apiCacheRepository, never()).save(any(ApiCacheEntity.class));
    }

    @Test
    void refreshesFromEwgfWhenStoredDatabaseMatchesAreStale() {
        Instant now = Instant.now();
        MatchEntity staleMatch = storedMatch("stale-stored-1", now.minus(20, ChronoUnit.MINUTES), "RANKED_BATTLE", 1, "Dragunov", "Bryan");
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                NORMALIZED_TEKKEN_ID,
                NORMALIZED_TEKKEN_ID)).thenReturn(List.of(staleMatch));
        when(matchRepository.findByExternalMatchKey(anyString())).thenReturn(Optional.empty());
        when(ewgfApiClient.getBattles(TEKKEN_ID)).thenReturn(ResponseEntity.ok("""
                {
                  "data": [
                    {
                      "id": "refreshed-1",
                      "battle_at": "%s",
                      "battle_type": "RANKED_BATTLE",
                      "winner": 1,
                      "p1_tekken_id": "%s",
                      "p1_name": "Me",
                      "p1_char": "Kazuya",
                      "p1_rounds_won": 3,
                      "p2_tekken_id": "opponent",
                      "p2_name": "Opponent",
                      "p2_char": "Jin",
                      "p2_rounds_won": 2
                    }
                  ]
                }
                """.formatted(now.toString(), TEKKEN_ID)));

        PlayerMatchesResponse response = playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null);

        assertThat(response.source()).isEqualTo("ewgf");
        assertThat(response.matches()).singleElement().satisfies(match -> {
            assertThat(match.externalMatchKey()).isEqualTo("refreshed-1");
            assertThat(match.my().character()).isEqualTo("Kazuya");
        });
        verify(ewgfApiClient).getBattles(TEKKEN_ID);
        verify(matchRepository).save(any(MatchEntity.class));
        verify(apiCacheRepository).save(any(ApiCacheEntity.class));
    }

    @Test
    void fallsBackToStoredDatabaseMatchesWhenEwgfBattlesFails() {
        Instant now = Instant.now();
        MatchEntity storedMatch = storedMatch("stored-1", now.minus(20, ChronoUnit.MINUTES), "RANKED_BATTLE", 1, "Dragunov", "Bryan");
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(ewgfApiClient.getBattles(TEKKEN_ID)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\":\"upstream failed\"}"));
        when(matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                NORMALIZED_TEKKEN_ID,
                NORMALIZED_TEKKEN_ID)).thenReturn(List.of(storedMatch));

        PlayerMatchesResponse response = playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null);

        assertThat(response.source()).isEqualTo("database");
        assertThat(response.total()).isEqualTo(1);
        assertThat(response.fetchedAt()).isEqualTo(storedMatch.getFetchedAt());
        assertThat(response.matches()).singleElement().satisfies(match -> {
            assertThat(match.externalMatchKey()).isEqualTo("stored-1");
            assertThat(match.my().character()).isEqualTo("Dragunov");
            assertThat(match.opponent().character()).isEqualTo("Bryan");
            assertThat(match.result()).isEqualTo("WIN");
        });
        verify(apiCacheRepository, never()).save(any(ApiCacheEntity.class));
    }

    @Test
    void calculatesStatsFromCachedMatchesWithFilters() {
        Instant now = Instant.now();
        ApiCacheEntity cache = new ApiCacheEntity(
                "ewgf:matches:" + NORMALIZED_TEKKEN_ID,
                "ewgf",
                Map.of("data", List.of(
                        battle("1", now.minus(1, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Dragunov", "Bryan"),
                        battle("2", now.minus(2, ChronoUnit.DAYS), "RANKED_BATTLE", 2, "Dragunov", "Bryan"),
                        battle("3", now.minus(2, ChronoUnit.DAYS), "RANKED_BATTLE", 1, "Kazuya", "Bryan"))),
                Instant.now().plus(10, ChronoUnit.MINUTES),
                now.minus(1, ChronoUnit.MINUTES));
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(cache));

        PlayerStatsResponse response = playerService.getStats(TEKKEN_ID, 100, "RANKED_BATTLE", "Dragunov", null, 30);

        assertThat(response.source()).isEqualTo("cache");
        assertThat(response.total()).isEqualTo(2);
        assertThat(response.wins()).isEqualTo(1);
        assertThat(response.losses()).isEqualTo(1);
        assertThat(response.winRate()).isEqualTo(50);
        assertThat(response.filters()).isEqualTo(new PlayerStatsFilters(100, "RANKED_BATTLE", "DRAGUNOV", null, 30));
        verifyNoInteractions(ewgfApiClient);
    }

    @Test
    void throwsPlayerApiExceptionWhenEwgfReturnsErrorStatus() {
        when(apiCacheRepository.findById("ewgf:matches:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(ewgfApiClient.getBattles(TEKKEN_ID)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\":\"upstream failed\"}"));

        assertThatThrownBy(() -> playerService.getMatches(TEKKEN_ID, 0, 12, null, null, null, null))
                .isInstanceOf(PlayerApiException.class)
                .hasMessage("EWGF API request failed: /external/battles/" + TEKKEN_ID)
                .satisfies(exception -> {
                    PlayerApiException playerApiException = (PlayerApiException) exception;
                    assertThat(playerApiException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(playerApiException.getCode()).isEqualTo("EWGF_REQUEST_FAILED");
                });
    }

    @Test
    void throwsBadGatewayWhenEwgfReturnsInvalidJson() {
        when(apiCacheRepository.findById("ewgf:profile:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.empty());
        when(ewgfApiClient.getProfile(TEKKEN_ID)).thenReturn(ResponseEntity.ok("not-json"));

        assertThatThrownBy(() -> playerService.getProfile(TEKKEN_ID))
                .isInstanceOf(PlayerApiException.class)
                .hasMessage("EWGF API returned invalid JSON: /external/profile/" + TEKKEN_ID)
                .satisfies(exception -> {
                    PlayerApiException playerApiException = (PlayerApiException) exception;
                    assertThat(playerApiException.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(playerApiException.getCode()).isEqualTo("EWGF_INVALID_JSON");
                });
    }

    @Test
    void recordsNormalizedTekkenIdInSearchHistory() {
        when(apiCacheRepository.findById("ewgf:profile:" + NORMALIZED_TEKKEN_ID)).thenReturn(Optional.of(
                new ApiCacheEntity(
                        "ewgf:profile:" + NORMALIZED_TEKKEN_ID,
                        "ewgf",
                        Map.of("data", profile("Cached Player", "Dragunov", "God of Destruction")),
                        Instant.now().plus(10, ChronoUnit.MINUTES),
                        Instant.now())));
        ArgumentCaptor<PlayerSearchHistoryEntity> captor = ArgumentCaptor.forClass(PlayerSearchHistoryEntity.class);

        playerService.getProfile(TEKKEN_ID);

        verify(searchHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getQuery()).isEqualTo(TEKKEN_ID);
        assertThat(captor.getValue().getTekkenId()).isEqualTo(NORMALIZED_TEKKEN_ID);
    }

    private static Map<String, Object> profile(String name, String character, String rank) {
        return Map.of(
                "nickname", name,
                "main_character", Map.of(character, rank));
    }

    private static Map<String, Object> battle(
            String id,
            Instant battleAt,
            String battleType,
            int winner,
            String myCharacter,
            String opponentCharacter
    ) {
        int myRounds = winner == 1 ? 3 : 2;
        int opponentRounds = winner == 1 ? 2 : 3;
        return Map.ofEntries(
                Map.entry("id", id),
                Map.entry("battle_at", battleAt.toString()),
                Map.entry("battle_type", battleType),
                Map.entry("winner", winner),
                Map.entry("p1_tekken_id", TEKKEN_ID),
                Map.entry("p1_name", "Me"),
                Map.entry("p1_char", myCharacter),
                Map.entry("p1_dan_rank", "God of Destruction"),
                Map.entry("p1_rounds_won", myRounds),
                Map.entry("p2_tekken_id", "opponent-" + id),
                Map.entry("p2_name", "Opponent " + id),
                Map.entry("p2_char", opponentCharacter),
                Map.entry("p2_dan_rank", "Tekken God"),
                Map.entry("p2_rounds_won", opponentRounds));
    }

    private static MatchEntity storedMatch(
            String id,
            Instant battleAt,
            String battleType,
            int winner,
            String myCharacter,
            String opponentCharacter
    ) {
        MatchEntity entity = new MatchEntity(id, battleAt);
        entity.updateFromBattle(battle(id, battleAt, battleType, winner, myCharacter, opponentCharacter), battleAt);
        return entity;
    }
}
