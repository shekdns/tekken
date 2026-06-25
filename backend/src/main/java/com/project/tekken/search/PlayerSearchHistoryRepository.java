package com.project.tekken.search;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerSearchHistoryRepository extends JpaRepository<PlayerSearchHistoryEntity, Long> {

    List<PlayerSearchHistoryEntity> findTop20ByOrderBySearchedAtDesc();

    List<PlayerSearchHistoryEntity> findByTekkenIdIsNotNullOrderBySearchedAtDesc(Pageable pageable);

    @Query("""
            select
                history.tekkenId as tekkenId,
                max(history.query) as query,
                count(history) as searchCount,
                max(history.searchedAt) as lastSearchedAt
            from PlayerSearchHistoryEntity history
            where history.tekkenId is not null
              and history.searchedAt >= :from
            group by history.tekkenId
            order by count(history) desc, max(history.searchedAt) desc
            """)
    List<SearchSuggestionProjection> findPopularSearchesSince(@Param("from") Instant from, Pageable pageable);
}
