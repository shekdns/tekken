package com.project.tekken.player.service;

import com.project.tekken.match.MatchEntity;
import com.project.tekken.match.MatchRepository;
import com.project.tekken.player.dto.PlayerMatchFilters;
import com.project.tekken.player.dto.PlayerMatchesResponse;
import com.project.tekken.player.mapper.PlayerMatchMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
class PlayerMatchQueryService {

    private static final String SOURCE_DATABASE = "database";

    private final MatchRepository matchRepository;

    PlayerMatchQueryService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    PlayerMatchData findStoredMatches(String normalizedTekkenId) {
        List<MatchEntity> storedMatches = matchRepository.findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(
                normalizedTekkenId,
                normalizedTekkenId);
        if (storedMatches == null || storedMatches.isEmpty()) {
            return null;
        }

        Instant fetchedAt = storedMatches.stream()
                .map(MatchEntity::getFetchedAt)
                .filter(value -> value != null)
                .max(Instant::compareTo)
                .orElseGet(Instant::now);

        return new PlayerMatchData(
                normalizedTekkenId,
                SOURCE_DATABASE,
                fetchedAt,
                storedMatches.stream()
                        .map(match -> PlayerMatchMapper.toSummary(match, normalizedTekkenId))
                        .toList());
    }

    PlayerMatchesResponse findStoredMatchesResponse(
            String normalizedTekkenId,
            int offset,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Integer days,
            Instant fromBattleAt
    ) {
        Optional<Instant> latestFetchedAt = matchRepository.findLatestFetchedAtByTekkenId(normalizedTekkenId);
        Instant fetchedAt = latestFetchedAt == null ? null : latestFetchedAt.orElse(null);
        if (fetchedAt == null) {
            return null;
        }

        Page<MatchEntity> page = matchRepository.findPlayerMatches(
                normalizedTekkenId,
                battleType,
                character,
                opponentCharacter,
                fromBattleAt,
                new OffsetPageRequest(limit, offset));
        int total = Math.toIntExact(page.getTotalElements());
        int nextOffset = offset + page.getNumberOfElements();
        boolean hasMore = nextOffset < total;

        return new PlayerMatchesResponse(
                normalizedTekkenId,
                SOURCE_DATABASE,
                fetchedAt,
                total,
                offset,
                limit,
                hasMore,
                hasMore ? nextOffset : null,
                new PlayerMatchFilters(limit, battleType, character, opponentCharacter, days),
                page.getContent().stream()
                        .map(match -> PlayerMatchMapper.toSummary(match, normalizedTekkenId))
                        .toList());
    }

    PlayerMatchData findStoredStatsData(
            String normalizedTekkenId,
            int limit,
            String battleType,
            String character,
            String opponentCharacter,
            Instant fromBattleAt
    ) {
        Optional<Instant> latestFetchedAt = matchRepository.findLatestFetchedAtByTekkenId(normalizedTekkenId);
        Instant fetchedAt = latestFetchedAt == null ? null : latestFetchedAt.orElse(null);
        if (fetchedAt == null) {
            return null;
        }

        Page<MatchEntity> page = matchRepository.findPlayerMatches(
                normalizedTekkenId,
                battleType,
                character,
                opponentCharacter,
                fromBattleAt,
                new OffsetPageRequest(limit, 0));
        if (page.isEmpty()) {
            return null;
        }

        return new PlayerMatchData(
                normalizedTekkenId,
                SOURCE_DATABASE,
                fetchedAt,
                page.getContent().stream()
                        .map(match -> PlayerMatchMapper.toSummary(match, normalizedTekkenId))
                        .toList());
    }
}
