package com.project.tekken.datasource.wavu;

import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WavuDataSourceService {

    private static final Duration PLAYER_SEARCH_CACHE_TTL = Duration.ofMinutes(10);

    private final WavuDataSourceClient wavuDataSourceClient;
    private final ApiCacheRepository apiCacheRepository;

    public WavuDataSourceService(
            WavuDataSourceClient wavuDataSourceClient,
            ApiCacheRepository apiCacheRepository
    ) {
        this.wavuDataSourceClient = wavuDataSourceClient;
        this.apiCacheRepository = apiCacheRepository;
    }

    public ResponseEntity<String> get(String path) {
        return wavuDataSourceClient.get(path);
    }

    @Transactional
    public List<WavuPlayerSearchResult> searchPlayers(String query) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        if (!wavuDataSourceClient.isEnabled()) {
            return List.of();
        }

        Instant now = Instant.now();
        String cacheKey = cacheKey(normalizedQuery);
        ApiCacheEntity cached = apiCacheRepository.findById(cacheKey)
                .filter(cache -> cache.isFresh(now))
                .orElse(null);
        if (cached != null) {
            return fromCache(cached.getResponseJson());
        }

        ResponseEntity<String> response = wavuDataSourceClient.searchPlayers(query);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            return List.of();
        }

        List<WavuPlayerSearchResult> results = parsePlayerSearchResults(response.getBody());
        saveCache(cacheKey, results, now);
        return results;
    }

    List<WavuPlayerSearchResult> parsePlayerSearchResults(String html) {
        Document document = Jsoup.parse(html);
        return document.select("#search-results tbody tr").stream()
                .map(this::toSearchResult)
                .filter(result -> result.tekkenId() != null && !result.tekkenId().isBlank())
                .toList();
    }

    private WavuPlayerSearchResult toSearchResult(Element row) {
        Element playerLink = row.selectFirst("td:first-child div:first-child a[href^=/player/]");
        Element tekkenIdNode = row.selectFirst("td:first-child div:nth-child(2)");
        Element platformNode = row.selectFirst("td:first-child div:nth-child(3) svg title");

        String tekkenId = tekkenIdFromLink(playerLink);
        return new WavuPlayerSearchResult(
                tekkenId,
                text(tekkenIdNode),
                text(playerLink),
                text(platformNode));
    }

    private String tekkenIdFromLink(Element playerLink) {
        if (playerLink == null) {
            return null;
        }
        String href = playerLink.attr("href");
        int index = href.lastIndexOf('/');
        return index >= 0 ? href.substring(index + 1) : href;
    }

    private String text(Element element) {
        if (element == null) {
            return null;
        }
        String text = element.text().trim();
        return text.isBlank() ? null : text;
    }

    private void saveCache(String cacheKey, List<WavuPlayerSearchResult> results, Instant now) {
        Map<String, Object> responseJson = toCache(results);
        Instant expiresAt = now.plus(PLAYER_SEARCH_CACHE_TTL);
        ApiCacheEntity cache = apiCacheRepository.findById(cacheKey)
                .orElseGet(() -> new ApiCacheEntity(cacheKey, "wavu", responseJson, expiresAt, now));
        cache.refresh(responseJson, expiresAt, now);
        apiCacheRepository.save(cache);
    }

    private Map<String, Object> toCache(List<WavuPlayerSearchResult> results) {
        List<Map<String, Object>> items = results.stream()
                .map(this::toCacheItem)
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        return response;
    }

    private Map<String, Object> toCacheItem(WavuPlayerSearchResult result) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tekkenId", result.tekkenId());
        item.put("displayTekkenId", result.displayTekkenId());
        item.put("name", result.name());
        item.put("platform", result.platform());
        return item;
    }

    private List<WavuPlayerSearchResult> fromCache(Map<String, Object> responseJson) {
        Object itemsValue = responseJson.get("items");
        if (!(itemsValue instanceof List<?> items)) {
            return List.of();
        }

        List<WavuPlayerSearchResult> results = new ArrayList<>();
        for (Object itemValue : items) {
            if (itemValue instanceof Map<?, ?> item) {
                String tekkenId = stringValue(item.get("tekkenId"));
                if (tekkenId != null && !tekkenId.isBlank()) {
                    results.add(new WavuPlayerSearchResult(
                            tekkenId,
                            stringValue(item.get("displayTekkenId")),
                            stringValue(item.get("name")),
                            stringValue(item.get("platform"))));
                }
            }
        }
        return results;
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private String cacheKey(String normalizedQuery) {
        return "wavu:player-search:" + normalizedQuery;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
