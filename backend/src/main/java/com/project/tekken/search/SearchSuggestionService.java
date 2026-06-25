package com.project.tekken.search;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SearchSuggestionService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_DAYS = 7;
    private static final int MAX_DAYS = 365;

    private final PlayerSearchHistoryRepository searchHistoryRepository;

    public SearchSuggestionService(PlayerSearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public SearchSuggestionResponse recent(int limit) {
        int normalizedLimit = normalizeLimit(limit);
        List<PlayerSearchHistoryEntity> histories = searchHistoryRepository.findByTekkenIdIsNotNullOrderBySearchedAtDesc(
                PageRequest.of(0, Math.min(normalizedLimit * 5, 200)));
        Map<String, SearchSuggestionItem> uniqueItems = new LinkedHashMap<>();

        for (PlayerSearchHistoryEntity history : histories) {
            if (uniqueItems.size() >= normalizedLimit) {
                break;
            }
            if (history.getTekkenId() == null || history.getTekkenId().isBlank()) {
                continue;
            }
            uniqueItems.putIfAbsent(history.getTekkenId(), new SearchSuggestionItem(
                    history.getTekkenId(),
                    displayTekkenId(history.getTekkenId(), history.getQuery()),
                    history.getQuery(),
                    1,
                    history.getSearchedAt()));
        }

        return new SearchSuggestionResponse(List.copyOf(uniqueItems.values()));
    }

    public SearchSuggestionResponse popular(int days, int limit) {
        int normalizedLimit = normalizeLimit(limit);
        int normalizedDays = normalizeDays(days);
        Instant from = Instant.now().minus(normalizedDays, ChronoUnit.DAYS);
        List<SearchSuggestionProjection> results = searchHistoryRepository.findPopularSearchesSince(
                from,
                PageRequest.of(0, normalizedLimit));

        return new SearchSuggestionResponse(results.stream()
                .map(result -> new SearchSuggestionItem(
                        result.getTekkenId(),
                        displayTekkenId(result.getTekkenId(), result.getQuery()),
                        result.getQuery(),
                        result.getSearchCount(),
                        result.getLastSearchedAt()))
                .toList());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int normalizeDays(int days) {
        if (days <= 0) {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }

    private String displayTekkenId(String tekkenId, String query) {
        if (query != null && !query.isBlank()) {
            return query;
        }
        return tekkenId;
    }
}
