package com.project.tekken.search;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerSearchHistoryRepository extends JpaRepository<PlayerSearchHistoryEntity, Long> {

    List<PlayerSearchHistoryEntity> findTop20ByOrderBySearchedAtDesc();
}
