package com.project.tekken.player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByTekkenId(String tekkenId);

    List<PlayerEntity> findByTekkenProwessIsNotNullOrderByTekkenProwessDescFetchedAtDesc(Pageable pageable);

    List<PlayerEntity> findByOrderByFetchedAtDesc(Pageable pageable);

    List<PlayerEntity> findByTekkenIdIn(Collection<String> tekkenIds);
}
