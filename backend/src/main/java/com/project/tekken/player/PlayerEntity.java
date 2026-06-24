package com.project.tekken.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import com.project.tekken.player.dto.PlayerProfileSummary;
import com.project.tekken.player.mapper.PlayerProfileMapper;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tekken_id", nullable = false, unique = true, length = 32)
    private String tekkenId;

    @Column(name = "name")
    private String name;

    @Column(name = "platform", length = 64)
    private String platform;

    @Column(name = "platform_id", length = 128)
    private String platformId;

    @Column(name = "language", length = 32)
    private String language;

    @Column(name = "region", length = 64)
    private String region;

    @Column(name = "tekken_prowess")
    private Integer tekkenProwess;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "main_character_json", columnDefinition = "jsonb")
    private Map<String, Object> mainCharacterJson;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_profile_json", columnDefinition = "jsonb")
    private Map<String, Object> rawProfileJson;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlayerEntity() {
    }

    public PlayerEntity(String tekkenId, Instant now) {
        this.tekkenId = tekkenId;
        this.createdAt = now;
        this.updatedAt = now;
        this.fetchedAt = now;
    }

    public void updateFromProfile(Map<String, Object> profile, Instant now) {
        PlayerProfileSummary summary = PlayerProfileMapper.toSummary(profile);
        this.name = summary.name();
        this.platform = summary.platform();
        this.platformId = PlayerProfileMapper.text(profile, "platform_id", "platformId", "account.platformId");
        this.language = summary.language();
        this.region = summary.region();
        this.tekkenProwess = summary.tekkenProwess();
        this.mainCharacterJson = PlayerProfileMapper.object(
                profile,
                "main_character",
                "mainCharacter",
                "favorite_character",
                "favoriteCharacter",
                "most_played_character",
                "mostPlayedCharacter",
                "character");
        this.lastSeen = summary.lastSeen();
        this.rawProfileJson = profile;
        this.fetchedAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getTekkenId() {
        return tekkenId;
    }

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPlatformId() {
        return platformId;
    }

    public String getLanguage() {
        return language;
    }

    public String getRegion() {
        return region;
    }

    public Integer getTekkenProwess() {
        return tekkenProwess;
    }

    public Map<String, Object> getMainCharacterJson() {
        return mainCharacterJson;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public Map<String, Object> getRawProfileJson() {
        return rawProfileJson;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
