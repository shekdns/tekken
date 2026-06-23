package com.project.tekken.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import com.project.tekken.external.EwgfApiClient;
import com.project.tekken.match.MatchEntity;
import com.project.tekken.match.MatchRepository;
import com.project.tekken.search.PlayerSearchHistoryEntity;
import com.project.tekken.search.PlayerSearchHistoryRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private static final String SOURCE_EWGF = "ewgf";
    private static final String SOURCE_CACHE = "cache";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final EwgfApiClient ewgfApiClient;
    private final ObjectMapper objectMapper;
    private final ApiCacheRepository apiCacheRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PlayerSearchHistoryRepository searchHistoryRepository;

    public PlayerService(
            EwgfApiClient ewgfApiClient,
            ObjectMapper objectMapper,
            ApiCacheRepository apiCacheRepository,
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            PlayerSearchHistoryRepository searchHistoryRepository
    ) {
        this.ewgfApiClient = ewgfApiClient;
        this.objectMapper = objectMapper;
        this.apiCacheRepository = apiCacheRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Transactional
    public PlayerProfileResponse getProfile(String tekkenId) {
        Instant now = Instant.now();
        String normalizedTekkenId = normalizeTekkenId(tekkenId);
        searchHistoryRepository.save(new PlayerSearchHistoryEntity(tekkenId, normalizedTekkenId, now));

        String cacheKey = cacheKey("profile", normalizedTekkenId);
        ApiCacheEntity cached = freshCache(cacheKey, now);
        if (cached != null) {
            Map<String, Object> profile = profileData(cached.getResponseJson());
            return new PlayerProfileResponse(
                    normalizedTekkenId,
                    SOURCE_CACHE,
                    cached.getUpdatedAt(),
                    PlayerProfileMapper.toSummary(profile),
                    profile);
        }

        ResponseEntity<String> response = ewgfApiClient.getProfile(tekkenId);
        Map<String, Object> body = parseJsonObject(response.getBody(), "/external/profile/" + tekkenId);
        assertSuccess(response, "/external/profile/" + tekkenId);

        Map<String, Object> profile = profileData(body);
        PlayerEntity player = playerRepository.findByTekkenId(normalizedTekkenId)
                .orElseGet(() -> new PlayerEntity(normalizedTekkenId, now));
        player.updateFromProfile(profile, now);
        playerRepository.save(player);
        saveCache(cacheKey, body, now);

        return new PlayerProfileResponse(
                normalizedTekkenId,
                SOURCE_EWGF,
                now,
                PlayerProfileMapper.toSummary(profile),
                profile);
    }

    @Transactional
    public PlayerMatchesResponse getMatches(String tekkenId) {
        Instant now = Instant.now();
        String normalizedTekkenId = normalizeTekkenId(tekkenId);

        String cacheKey = cacheKey("matches", normalizedTekkenId);
        ApiCacheEntity cached = freshCache(cacheKey, now);
        if (cached != null) {
            List<Map<String, Object>> battles = battleData(cached.getResponseJson());
            return new PlayerMatchesResponse(
                    normalizedTekkenId,
                    SOURCE_CACHE,
                    cached.getUpdatedAt(),
                    matchSummaries(battles, normalizedTekkenId));
        }

        ResponseEntity<String> response = ewgfApiClient.getBattles(tekkenId);
        Map<String, Object> body = parseJsonObject(response.getBody(), "/external/battles/" + tekkenId);
        assertSuccess(response, "/external/battles/" + tekkenId);

        List<Map<String, Object>> battles = battleData(body);
        saveMatches(battles, now);
        saveCache(cacheKey, body, now);

        return new PlayerMatchesResponse(
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
            throw new PlayerApiException(HttpStatus.BAD_GATEWAY, "EWGF API returned an empty response: " + path);
        }
        try {
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new PlayerApiException(HttpStatus.BAD_GATEWAY, "EWGF API returned invalid JSON: " + path);
        }
    }

    private void assertSuccess(ResponseEntity<String> response, String path) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new PlayerApiException(
                    response.getStatusCode(),
                    "EWGF API request failed: " + path);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> profileData(Map<String, Object> body) {
        Object data = body.get("data");
        return data instanceof Map<?, ?> ? (Map<String, Object>) data : body;
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
