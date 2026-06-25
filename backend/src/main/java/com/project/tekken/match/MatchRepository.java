package com.project.tekken.match;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<MatchEntity, Long>, JpaSpecificationExecutor<MatchEntity> {

    Optional<MatchEntity> findByExternalMatchKey(String externalMatchKey);

    List<MatchEntity> findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(String p1TekkenId, String p2TekkenId);

    default Page<MatchEntity> findPlayerMatches(
            String tekkenId,
            String battleType,
            String character,
            String opponentCharacter,
            Instant fromBattleAt,
            Pageable pageable
    ) {
        return findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("p1TekkenId"), tekkenId),
                    criteriaBuilder.equal(root.get("p2TekkenId"), tekkenId)));

            if (battleType != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("battleType")), battleType));
            }
            if (character != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("p1TekkenId"), tekkenId),
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get("p1Char")), character)),
                        criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("p2TekkenId"), tekkenId),
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get("p2Char")), character))));
            }
            if (opponentCharacter != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("p1TekkenId"), tekkenId),
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get("p2Char")), opponentCharacter)),
                        criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("p2TekkenId"), tekkenId),
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get("p1Char")), opponentCharacter))));
            }
            if (fromBattleAt != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("battleAt"), fromBattleAt));
            }
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(criteriaBuilder.desc(root.get("battleAt")));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    @Query("""
            select max(match.fetchedAt)
            from MatchEntity match
            where match.p1TekkenId = :tekkenId or match.p2TekkenId = :tekkenId
            """)
    Optional<Instant> findLatestFetchedAtByTekkenId(@Param("tekkenId") String tekkenId);
}
