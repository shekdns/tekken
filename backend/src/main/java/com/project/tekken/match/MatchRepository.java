package com.project.tekken.match;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    Optional<MatchEntity> findByExternalMatchKey(String externalMatchKey);

    List<MatchEntity> findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(String p1TekkenId, String p2TekkenId);

    @Query("""
            select match
            from MatchEntity match
            where (match.p1TekkenId = :tekkenId or match.p2TekkenId = :tekkenId)
              and (:battleType is null or upper(match.battleType) = :battleType)
              and (:character is null or (
                    (match.p1TekkenId = :tekkenId and upper(match.p1Char) = :character)
                    or (match.p2TekkenId = :tekkenId and upper(match.p2Char) = :character)
              ))
              and (:opponentCharacter is null or (
                    (match.p1TekkenId = :tekkenId and upper(match.p2Char) = :opponentCharacter)
                    or (match.p2TekkenId = :tekkenId and upper(match.p1Char) = :opponentCharacter)
              ))
              and (:fromBattleAt is null or match.battleAt >= :fromBattleAt)
            order by match.battleAt desc
            """)
    Page<MatchEntity> findPlayerMatches(
            @Param("tekkenId") String tekkenId,
            @Param("battleType") String battleType,
            @Param("character") String character,
            @Param("opponentCharacter") String opponentCharacter,
            @Param("fromBattleAt") Instant fromBattleAt,
            Pageable pageable);

    @Query("""
            select max(match.fetchedAt)
            from MatchEntity match
            where match.p1TekkenId = :tekkenId or match.p2TekkenId = :tekkenId
            """)
    Optional<Instant> findLatestFetchedAtByTekkenId(@Param("tekkenId") String tekkenId);
}
