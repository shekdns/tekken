package com.project.tekken.cache;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCacheRepository extends JpaRepository<ApiCacheEntity, String> {

    List<ApiCacheEntity> findByExpiresAtBefore(Instant now);
}
