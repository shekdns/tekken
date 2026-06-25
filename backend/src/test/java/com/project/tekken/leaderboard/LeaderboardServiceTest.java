package com.project.tekken.leaderboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import com.project.tekken.search.PlayerSearchHistoryRepository;
import com.project.tekken.search.SearchSuggestionProjection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void returnsProwessLeaderboardWithRanks() {
        Instant now = Instant.now();
        when(playerRepository.findByTekkenProwessIsNotNullOrderByTekkenProwessDescFetchedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(
                        player("aaa", "Alpha", "Dragunov", "God of Destruction", 300000, "KR", "Steam", now),
                        player("bbb", "Bravo", "Jin", "Tekken King", 250000, "JP", "PSN", now.minusSeconds(60))));
        when(searchHistoryRepository.findPopularSearchesSince(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(new Projection("aaa", 4), new Projection("bbb", 2)));

        PlayerLeaderboardResponse response = leaderboardService.players(
                new LeaderboardFilters("prowess", null, null, null, 10));

        assertThat(response.source()).isEqualTo("t8lab");
        assertThat(response.filters().sort()).isEqualTo("prowess");
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).rank()).isEqualTo(1);
        assertThat(response.items().get(0).tekkenId()).isEqualTo("aaa");
        assertThat(response.items().get(0).mainCharacter()).isEqualTo("Dragunov");
        assertThat(response.items().get(0).danRank()).isEqualTo("God of Destruction");
        assertThat(response.items().get(0).searchCount()).isEqualTo(4);
        assertThat(response.items().get(1).rank()).isEqualTo(2);
    }

    @Test
    void filtersByCharacterRegionAndPlatform() {
        Instant now = Instant.now();
        when(playerRepository.findByTekkenProwessIsNotNullOrderByTekkenProwessDescFetchedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(
                        player("aaa", "Alpha", "Dragunov", "God of Destruction", 300000, "KR", "Steam", now),
                        player("bbb", "Bravo", "Jin", "Tekken King", 250000, "JP", "PSN", now.minusSeconds(60))));
        when(searchHistoryRepository.findPopularSearchesSince(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of());

        PlayerLeaderboardResponse response = leaderboardService.players(
                new LeaderboardFilters("prowess", "drag", "kr", "steam", 10));

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.tekkenId()).isEqualTo("aaa");
            assertThat(item.rank()).isEqualTo(1);
        });
    }

    @Test
    void returnsSearchCountLeaderboardInPopularOrder() {
        Instant now = Instant.now();
        when(searchHistoryRepository.findPopularSearchesSince(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(new Projection("bbb", 9), new Projection("aaa", 4)));
        when(playerRepository.findByTekkenIdIn(List.of("bbb", "aaa")))
                .thenReturn(List.of(
                        player("aaa", "Alpha", "Dragunov", "God of Destruction", 300000, "KR", "Steam", now),
                        player("bbb", "Bravo", "Jin", "Tekken King", 250000, "JP", "PSN", now.minusSeconds(60))));

        PlayerLeaderboardResponse response = leaderboardService.players(
                new LeaderboardFilters("searches", null, null, null, 10));

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).tekkenId()).isEqualTo("bbb");
        assertThat(response.items().get(0).searchCount()).isEqualTo(9);
        assertThat(response.items().get(1).tekkenId()).isEqualTo("aaa");
        assertThat(response.items().get(1).searchCount()).isEqualTo(4);
    }

    private PlayerEntity player(
            String tekkenId,
            String name,
            String character,
            String rank,
            int prowess,
            String region,
            String platform,
            Instant now
    ) {
        PlayerEntity player = new PlayerEntity(tekkenId, now);
        player.updateFromProfile(Map.of(
                "name", name,
                "region", region,
                "platform", platform,
                "tekkenProwess", prowess,
                "mainCharacter", Map.of(character, rank)), now);
        return player;
    }

    private record Projection(String tekkenId, long searchCount) implements SearchSuggestionProjection {

        @Override
        public String getTekkenId() {
            return tekkenId;
        }

        @Override
        public String getQuery() {
            return tekkenId;
        }

        @Override
        public long getSearchCount() {
            return searchCount;
        }

        @Override
        public Instant getLastSearchedAt() {
            return Instant.now();
        }
    }
}
