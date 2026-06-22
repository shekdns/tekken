package com.project.tekken.match;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    Optional<MatchEntity> findByExternalMatchKey(String externalMatchKey);

    List<MatchEntity> findByP1TekkenIdOrP2TekkenIdOrderByBattleAtDesc(String p1TekkenId, String p2TekkenId);
}
