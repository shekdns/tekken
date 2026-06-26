package com.project.tekken.search;

import com.project.tekken.datasource.wavu.WavuDataSourceService;
import com.project.tekken.datasource.wavu.WavuPlayerSearchResult;
import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import com.project.tekken.player.mapper.PlayerProfileMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PlayerSearchAutocompleteService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int MIN_QUERY_LENGTH = 2;

    private final PlayerRepository playerRepository;
    private final PlayerSearchHistoryRepository searchHistoryRepository;
    private final WavuDataSourceService wavuDataSourceService;

    public PlayerSearchAutocompleteService(
            PlayerRepository playerRepository,
            PlayerSearchHistoryRepository searchHistoryRepository,
            WavuDataSourceService wavuDataSourceService
    ) {
        this.playerRepository = playerRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.wavuDataSourceService = wavuDataSourceService;
    }

    public PlayerSearchAutocompleteResponse search(String query, int limit) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.length() < MIN_QUERY_LENGTH) {
            return new PlayerSearchAutocompleteResponse(List.of());
        }

        int normalizedLimit = normalizeLimit(limit);
        Map<String, PlayerSearchAutocompleteItem> items = new LinkedHashMap<>();
        int candidateSize = Math.min(normalizedLimit * 3, 60);

        List<PlayerEntity> players = playerRepository.findAutocompleteCandidates(
                normalizedQuery,
                normalizeTekkenId(normalizedQuery),
                PageRequest.of(0, candidateSize));
        for (PlayerEntity player : players) {
            if (items.size() >= normalizedLimit) {
                break;
            }
            items.putIfAbsent(player.getTekkenId(), fromPlayer(player));
        }

        if (items.size() < normalizedLimit) {
            List<PlayerSearchHistoryEntity> histories = searchHistoryRepository.findAutocompleteCandidates(
                    normalizedQuery,
                    normalizeTekkenId(normalizedQuery),
                    PageRequest.of(0, candidateSize));
            for (PlayerSearchHistoryEntity history : histories) {
                if (items.size() >= normalizedLimit) {
                    break;
                }
                if (history.getTekkenId() == null || history.getTekkenId().isBlank()) {
                    continue;
                }
                items.putIfAbsent(history.getTekkenId(), fromHistory(history));
            }
        }

        if (items.size() < normalizedLimit) {
            List<WavuPlayerSearchResult> wavuResults = wavuDataSourceService.searchPlayers(normalizedQuery);
            for (WavuPlayerSearchResult result : wavuResults) {
                if (items.size() >= normalizedLimit) {
                    break;
                }
                if (result.tekkenId() == null || result.tekkenId().isBlank()) {
                    continue;
                }
                items.putIfAbsent(result.tekkenId(), fromWavu(result));
            }
        }

        return new PlayerSearchAutocompleteResponse(List.copyOf(items.values()));
    }

    private PlayerSearchAutocompleteItem fromPlayer(PlayerEntity player) {
        return new PlayerSearchAutocompleteItem(
                player.getTekkenId(),
                player.getTekkenId(),
                player.getName(),
                mainCharacter(player),
                danRank(player),
                player.getTekkenProwess(),
                player.getRegion(),
                player.getPlatform(),
                "t8lab",
                player.getFetchedAt());
    }

    private PlayerSearchAutocompleteItem fromHistory(PlayerSearchHistoryEntity history) {
        return new PlayerSearchAutocompleteItem(
                history.getTekkenId(),
                displayTekkenId(history.getTekkenId(), history.getQuery()),
                null,
                null,
                null,
                null,
                null,
                null,
                "search_history",
                history.getSearchedAt());
    }

    private PlayerSearchAutocompleteItem fromWavu(WavuPlayerSearchResult result) {
        return new PlayerSearchAutocompleteItem(
                result.tekkenId(),
                displayTekkenId(result.tekkenId(), result.displayTekkenId()),
                result.name(),
                null,
                null,
                null,
                null,
                result.platform(),
                "wavu",
                null);
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

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeTekkenId(String value) {
        return value.replace("-", "").replace(" ", "");
    }

    private String displayTekkenId(String tekkenId, String query) {
        if (query != null && !query.isBlank()) {
            return query;
        }
        return tekkenId;
    }
}
