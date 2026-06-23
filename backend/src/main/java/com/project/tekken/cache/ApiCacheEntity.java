package com.project.tekken.cache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "api_cache")
public class ApiCacheEntity {

    @Id
    @Column(name = "cache_key", length = 255)
    private String cacheKey;

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> responseJson;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ApiCacheEntity() {
    }

    public ApiCacheEntity(String cacheKey, String source, Map<String, Object> responseJson, Instant expiresAt, Instant now) {
        this.cacheKey = cacheKey;
        this.source = source;
        this.responseJson = responseJson;
        this.expiresAt = expiresAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void refresh(Map<String, Object> responseJson, Instant expiresAt, Instant now) {
        this.responseJson = responseJson;
        this.expiresAt = expiresAt;
        this.updatedAt = now;
    }

    public boolean isFresh(Instant now) {
        return expiresAt.isAfter(now);
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getResponseJson() {
        return responseJson;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
