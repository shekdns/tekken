package com.project.tekken.leaderboard;

import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import com.project.tekken.player.mapper.PlayerProfileMapper;
import com.project.tekken.search.PlayerSearchHistoryRepository;
import com.project.tekken.search.SearchSuggestionProjection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final int FETCH_MULTIPLIER = 5;

    private final PlayerRepository playerRepository;
    private final PlayerSearchHistoryRepository searchHistoryRepository;

    public LeaderboardService(
            PlayerRepository playerRepository,
            PlayerSearchHistoryRepository searchHistoryRepository
    ) {
        this.playerRepository = playerRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public PlayerLeaderboardResponse players(LeaderboardFilters filters) {
        LeaderboardFilters normalizedFilters = normalize(filters);
        List<PlayerEntity> players = switch (normalizedFilters.sort()) {
            case "recent" -> recentCandidates(normalizedFilters.limit());
            case "searches" -> searchedCandidates(normalizedFilters.limit());
            default -> prowessCandidates(normalizedFilters.limit());
        };

        Map<String, Long> searchCounts = searchCounts();
        List<PlayerLeaderboardItem> items = players.stream()
                .filter(player -> matchesFilters(player, normalizedFilters))
                .limit(normalizedFilters.limit())
                .map(player -> toItem(player, searchCounts.getOrDefault(player.getTekkenId(), 0L)))
                .toList();

        return new PlayerLeaderboardResponse(
                "t8lab",
                Instant.now(),
                normalizedFilters,
                rank(items));
    }

    private List<PlayerEntity> prowessCandidates(int limit) {
        return playerRepository.findByTekkenProwessIsNotNullOrderByTekkenProwessDescFetchedAtDesc(
                PageRequest.of(0, candidateSize(limit)));
    }

    private List<PlayerEntity> recentCandidates(int limit) {
        return playerRepository.findByOrderByFetchedAtDesc(PageRequest.of(0, candidateSize(limit)));
    }

    private List<PlayerEntity> searchedCandidates(int limit) {
        List<SearchSuggestionProjection> popular = searchHistoryRepository.findPopularSearchesSince(
                Instant.now().minus(365, ChronoUnit.DAYS),
                PageRequest.of(0, candidateSize(limit)));
        List<String> tekkenIds = popular.stream()
                .map(SearchSuggestionProjection::getTekkenId)
                .filter(Objects::nonNull)
                .toList();
        if (tekkenIds.isEmpty()) {
            return List.of();
        }

        Map<String, PlayerEntity> playersByTekkenId = new HashMap<>();
        for (PlayerEntity player : playerRepository.findByTekkenIdIn(tekkenIds)) {
            playersByTekkenId.put(player.getTekkenId(), player);
        }

        return tekkenIds.stream()
                .map(playersByTekkenId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Long> searchCounts() {
        List<SearchSuggestionProjection> popular = searchHistoryRepository.findPopularSearchesSince(
                Instant.now().minus(365, ChronoUnit.DAYS),
                PageRequest.of(0, MAX_LIMIT * FETCH_MULTIPLIER));
        Map<String, Long> counts = new LinkedHashMap<>();
        for (SearchSuggestionProjection item : popular) {
            counts.put(item.getTekkenId(), item.getSearchCount());
        }
        return counts;
    }

    private PlayerLeaderboardItem toItem(PlayerEntity player, long searchCount) {
        return new PlayerLeaderboardItem(
                0,
                player.getTekkenId(),
                player.getName(),
                mainCharacter(player),
                danRank(player),
                player.getTekkenProwess(),
                player.getRegion(),
                player.getPlatform(),
                searchCount,
                player.getFetchedAt());
    }

    private List<PlayerLeaderboardItem> rank(List<PlayerLeaderboardItem> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> {
                    PlayerLeaderboardItem item = items.get(index);
                    return new PlayerLeaderboardItem(
                            index + 1,
                            item.tekkenId(),
                            item.name(),
                            item.mainCharacter(),
                            item.danRank(),
                            item.tekkenProwess(),
                            item.region(),
                            item.platform(),
                            item.searchCount(),
                            item.lastUpdatedAt());
                })
                .toList();
    }

    private boolean matchesFilters(PlayerEntity player, LeaderboardFilters filters) {
        return matchesText(mainCharacter(player), filters.character())
                && matchesText(player.getRegion(), filters.region())
                && matchesText(player.getPlatform(), filters.platform());
    }

    private boolean matchesText(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private String mainCharacter(PlayerEntity player) {
        Map<String, Object> mainCharacter = player.getMainCharacterJson();
        if (mainCharacter == null || mainCharacter.isEmpty()) {
            return null;
        }

        String namedValue = PlayerProfileMapper.text(mainCharacter, "name", "character", "character_name", "characterName");
        if (namedValue != null) {
            return namedValue;
        }
        return mainCharacter.size() == 1 ? mainCharacter.keySet().iterator().next() : null;
    }

    private String danRank(PlayerEntity player) {
        Map<String, Object> mainCharacter = player.getMainCharacterJson();
        if (mainCharacter == null || mainCharacter.isEmpty()) {
            return null;
        }

        String rankValue = PlayerProfileMapper.text(
                mainCharacter,
                "rank",
                "rank_name",
                "rankName",
                "dan_rank",
                "danRank",
                "danRank.name");
        if (rankValue != null) {
            return rankValue;
        }
        return mainCharacter.size() == 1 ? String.valueOf(mainCharacter.values().iterator().next()) : null;
    }

    private LeaderboardFilters normalize(LeaderboardFilters filters) {
        String sort = filters.sort() == null || filters.sort().isBlank()
                ? "prowess"
                : filters.sort().toLowerCase(Locale.ROOT);
        if (!List.of("prowess", "recent", "searches").contains(sort)) {
            sort = "prowess";
        }
        int limit = filters.limit() <= 0 ? DEFAULT_LIMIT : Math.min(filters.limit(), MAX_LIMIT);
        return new LeaderboardFilters(
                sort,
                blankToNull(filters.character()),
                blankToNull(filters.region()),
                blankToNull(filters.platform()),
                limit);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private int candidateSize(int limit) {
        return Math.min(limit * FETCH_MULTIPLIER, MAX_LIMIT * FETCH_MULTIPLIER);
    }
}
