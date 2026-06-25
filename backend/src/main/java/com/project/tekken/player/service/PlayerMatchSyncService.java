package com.project.tekken.player.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import com.project.tekken.datasource.ewgf.EwgfApiClient;
import com.project.tekken.match.MatchEntity;
import com.project.tekken.match.MatchRepository;
import com.project.tekken.player.dto.PlayerMatchSummary;
import com.project.tekken.player.exception.PlayerApiException;
import com.project.tekken.player.mapper.PlayerMatchMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
class PlayerMatchSyncService {

    private static final String SOURCE_EWGF = "ewgf";
    private static final String SOURCE_CACHE = "cache";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration DB_SYNC_TTL = Duration.ofMinutes(10);

    private final EwgfApiClient ewgfApiClient;
    private final ObjectMapper objectMapper;
    private final ApiCacheRepository apiCacheRepository;
    private final MatchRepository matchRepository;
    private final PlayerMatchQueryService matchQueryService;

    PlayerMatchSyncService(
            EwgfApiClient ewgfApiClient,
            ObjectMapper objectMapper,
            ApiCacheRepository apiCacheRepository,
            MatchRepository matchRepository,
            PlayerMatchQueryService matchQueryService
    ) {
        this.ewgfApiClient = ewgfApiClient;
        this.objectMapper = objectMapper;
        this.apiCacheRepository = apiCacheRepository;
        this.matchRepository = matchRepository;
        this.matchQueryService = matchQueryService;
    }

    PlayerMatchData getMatchesData(String tekkenId) {
        return getMatchesData(tekkenId, false);
    }

    PlayerMatchData getMatchesData(String tekkenId, boolean refresh) {
        Instant now = Instant.now();
        String normalizedTekkenId = normalizeTekkenId(tekkenId);

        String cacheKey = cacheKey("matches", normalizedTekkenId);
        ApiCacheEntity cached = refresh ? null : freshCache(cacheKey, now);
        if (cached != null) {
            List<Map<String, Object>> battles = battleData(cached.getResponseJson());
            return new PlayerMatchData(
                    normalizedTekkenId,
                    SOURCE_CACHE,
                    cached.getUpdatedAt(),
                    matchSummaries(battles, normalizedTekkenId));
        }

        PlayerMatchData storedMatches = matchQueryService.findStoredMatches(normalizedTekkenId);
        if (!refresh && storedMatches != null && storedMatches.isFresh(now, DB_SYNC_TTL)) {
            return storedMatches;
        }

        String path = "/external/battles/" + tekkenId;
        ResponseEntity<String> response = ewgfApiClient.getBattles(tekkenId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            if (storedMatches != null) {
                return storedMatches;
            }
            assertSuccess(response, path);
        }
        Map<String, Object> body = parseJsonObject(response.getBody(), path);

        List<Map<String, Object>> battles = battleData(body);
        saveMatches(battles, now);
        saveCache(cacheKey, body, now);

        return new PlayerMatchData(
                normalizedTekkenId,
                SOURCE_EWGF,
                now,
                matchSummaries(battles, normalizedTekkenId));
    }

    private ApiCacheEntity freshCache(String cacheKey, Instant now) {
        return apiCacheRepository.findById(cacheKey)
                .filter(cache -> cache.isFresh(now))
                .orElse(null);
    }

    private void saveCache(String cacheKey, Map<String, Object> responseJson, Instant now) {
        ApiCacheEntity cache = apiCacheRepository.findById(cacheKey)
                .orElseGet(() -> new ApiCacheEntity(cacheKey, SOURCE_EWGF, responseJson, now.plus(CACHE_TTL), now));
        cache.refresh(responseJson, now.plus(CACHE_TTL), now);
        apiCacheRepository.save(cache);
    }

    private void saveMatches(List<Map<String, Object>> battles, Instant now) {
        for (Map<String, Object> battle : battles) {
            String externalKey = MatchEntity.externalKey(battle);
            if (externalKey.isBlank()) {
                continue;
            }

            MatchEntity match = matchRepository.findByExternalMatchKey(externalKey)
                    .orElseGet(() -> new MatchEntity(externalKey, now));
            match.updateFromBattle(battle, now);
            if (match.hasBattleAt()) {
                matchRepository.save(match);
            }
        }
    }

    private List<PlayerMatchSummary> matchSummaries(List<Map<String, Object>> battles, String tekkenId) {
        return battles.stream()
                .map(battle -> PlayerMatchMapper.toSummary(battle, tekkenId))
                .toList();
    }

    private Map<String, Object> parseJsonObject(String body, String path) {
        if (body == null || body.isBlank()) {
            throw new PlayerApiException(HttpStatus.BAD_GATEWAY, "EWGF_EMPTY_RESPONSE", "EWGF API returned an empty response: " + path);
        }
        try {
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new PlayerApiException(HttpStatus.BAD_GATEWAY, "EWGF_INVALID_JSON", "EWGF API returned invalid JSON: " + path);
        }
    }

    private void assertSuccess(ResponseEntity<String> response, String path) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new PlayerApiException(
                    response.getStatusCode(),
                    "EWGF_REQUEST_FAILED",
                    "EWGF API request failed: " + path);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> battleData(Map<String, Object> body) {
        Object data = body.get("data");
        if (!(data instanceof List<?> values)) {
            return List.of();
        }

        List<Map<String, Object>> battles = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Map<?, ?> map) {
                battles.add((Map<String, Object>) map);
            }
        }
        return battles;
    }

    private String cacheKey(String type, String tekkenId) {
        return SOURCE_EWGF + ":" + type + ":" + tekkenId;
    }

    private String normalizeTekkenId(String tekkenId) {
        return tekkenId.replace("-", "");
    }
}
