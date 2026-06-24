package com.project.tekken.player.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import com.project.tekken.datasource.ewgf.EwgfApiClient;
import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import com.project.tekken.player.dto.PlayerMatchFilters;
import com.project.tekken.player.dto.PlayerMatchesResponse;
import com.project.tekken.player.dto.PlayerMatchSummary;
import com.project.tekken.player.dto.PlayerProfileResponse;
import com.project.tekken.player.dto.PlayerStatsFilters;
import com.project.tekken.player.dto.PlayerStatsResponse;
import com.project.tekken.player.exception.PlayerApiException;
import com.project.tekken.player.mapper.PlayerProfileMapper;
import com.project.tekken.player.stats.PlayerStatsCalculator;
import com.project.tekken.search.PlayerSearchHistoryEntity;
import com.project.tekken.search.PlayerSearchHistoryRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
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
    private final PlayerSearchHistoryRepository searchHistoryRepository;
    private final PlayerMatchSyncService matchSyncService;
    private final PlayerMatchQueryService matchQueryService;

    public PlayerService(
            EwgfApiClient ewgfApiClient,
            ObjectMapper objectMapper,
            ApiCacheRepository apiCacheRepository,
            PlayerRepository playerRepository,
            PlayerSearchHistoryRepository searchHistoryRepository,
            PlayerMatchSyncService matchSyncService,
            PlayerMatchQueryService matchQueryService
    ) {
        this.ewgfApiClient = ewgfApiClient;
        this.objectMapper = objectMapper;
        this.apiCacheRepository = apiCacheRepository;
        this.playerRepository = playerRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.matchSyncService = matchSyncService;
        this.matchQueryService = matchQueryService;
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
    public PlayerMatchesResponse getMatches(
            String tekkenId,
            int offset,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days
    ) {
        return getMatches(tekkenId, offset, limit, battleType, character, opponentCharacter, days, false);
    }

    @Transactional
    public PlayerMatchesResponse getMatches(
            String tekkenId,
            int offset,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days,
            boolean refresh
    ) {
        PlayerMatchData data = matchSyncService.getMatchesData(tekkenId, refresh);
        int normalizedOffset = normalizeOffset(offset);
        int normalizedLimit = normalizePageLimit(limit);
        String normalizedBattleType = normalizeBattleType(battleType);
        String normalizedCharacter = normalizeTextFilter(character);
        String normalizedOpponentCharacter = normalizeTextFilter(opponentCharacter);
        Integer normalizedDays = normalizeDays(days);
        PlayerMatchesResponse storedResponse = matchQueryService.findStoredMatchesResponse(
                data.tekkenId(),
                normalizedOffset,
                normalizedLimit,
                normalizedBattleType,
                normalizedCharacter,
                normalizedOpponentCharacter,
                normalizedDays,
                fromBattleAt(normalizedDays));
        if (storedResponse != null) {
            return storedResponse;
        }
        return pagedMatchesResponse(data, offset, limit, battleType, character, opponentCharacter, days);
    }

    @Transactional
    public PlayerStatsResponse getStats(
            String tekkenId,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days
    ) {
        PlayerMatchData matches = matchSyncService.getMatchesData(tekkenId);
        return PlayerStatsCalculator.fromMatches(
                matches.tekkenId(),
                matches.source(),
                matches.fetchedAt(),
                matches.matches(),
                limit,
                battleType,
                character,
                opponentCharacter,
                days);
    }

    private PlayerMatchesResponse pagedMatchesResponse(
            PlayerMatchData data,
            int offset,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days
    ) {
        int normalizedOffset = normalizeOffset(offset);
        int normalizedLimit = normalizePageLimit(limit);
        String normalizedBattleType = normalizeBattleType(battleType);
        String normalizedCharacter = normalizeTextFilter(character);
        String normalizedOpponentCharacter = normalizeTextFilter(opponentCharacter);
        Integer normalizedDays = normalizeDays(days);
        List<PlayerMatchSummary> filteredMatches = filteredMatches(
                data.matches(),
                normalizedBattleType,
                normalizedCharacter,
                normalizedOpponentCharacter,
                normalizedDays);
        int total = filteredMatches.size();
        int fromIndex = Math.min(normalizedOffset, total);
        int toIndex = Math.min(fromIndex + normalizedLimit, total);
        boolean hasMore = toIndex < total;

        return new PlayerMatchesResponse(
                data.tekkenId(),
                data.source(),
                data.fetchedAt(),
                total,
                normalizedOffset,
                normalizedLimit,
                hasMore,
                hasMore ? toIndex : null,
                new PlayerMatchFilters(
                        normalizedLimit,
                        normalizedBattleType,
                        normalizedCharacter,
                        normalizedOpponentCharacter,
                        normalizedDays),
                filteredMatches.subList(fromIndex, toIndex));
    }

    private List<PlayerMatchSummary> filteredMatches(
            List<PlayerMatchSummary> matches,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days
    ) {
        Instant fromBattleAt = fromBattleAt(days);
        return matches.stream()
                .filter(match -> battleType == null || battleType.equals(normalizeBattleType(match.battleType())))
                .filter(match -> character == null || character.equals(normalizeTextFilter(match.my() == null ? null : match.my().character())))
                .filter(match -> opponentCharacter == null || opponentCharacter.equals(normalizeTextFilter(match.opponent() == null ? null : match.opponent().character())))
                .filter(match -> fromBattleAt == null || (match.battleAt() != null && !match.battleAt().isBefore(fromBattleAt)))
                .toList();
    }

    private int normalizeOffset(int offset) {
        return Math.max(offset, 0);
    }

    private int normalizePageLimit(int limit) {
        if (limit <= 0) {
            return 12;
        }
        return Math.min(limit, 50);
    }

    private String normalizeBattleType(String battleType) {
        if (battleType == null || battleType.isBlank() || "ALL".equalsIgnoreCase(battleType)) {
            return null;
        }
        return battleType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeTextFilter(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private Integer normalizeDays(Integer days) {
        if (days == null || days <= 0) {
            return null;
        }
        return Math.min(days, 365);
    }

    private Instant fromBattleAt(Integer days) {
        return days == null ? null : Instant.now().minus(days, ChronoUnit.DAYS);
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

    private String cacheKey(String type, String tekkenId) {
        return SOURCE_EWGF + ":" + type + ":" + tekkenId;
    }

    private String normalizeTekkenId(String tekkenId) {
        return tekkenId.replace("-", "");
    }

}
