package com.project.tekken.player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByTekkenId(String tekkenId);

    List<PlayerEntity> findByTekkenProwessIsNotNullOrderByTekkenProwessDescFetchedAtDesc(Pageable pageable);

    List<PlayerEntity> findByOrderByFetchedAtDesc(Pageable pageable);

    List<PlayerEntity> findByTekkenIdIn(Collection<String> tekkenIds);

    @Query("""
            select player
            from PlayerEntity player
            where lower(replace(player.tekkenId, '-', '')) like concat('%', :normalizedQuery, '%')
               or lower(player.tekkenId) like concat('%', :query, '%')
               or lower(coalesce(player.name, '')) like concat('%', :query, '%')
            order by player.fetchedAt desc
            """)
    List<PlayerEntity> findAutocompleteCandidates(
            @Param("query") String query,
            @Param("normalizedQuery") String normalizedQuery,
            Pageable pageable);
}
