package com.project.tekken.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_match_key", nullable = false, unique = true, length = 128)
    private String externalMatchKey;

    @Column(name = "battle_at", nullable = false)
    private Instant battleAt;

    @Column(name = "battle_type", length = 64)
    private String battleType;

    @Column(name = "game_version")
    private Integer gameVersion;

    @Column(name = "stage_id")
    private Integer stageId;

    @Column(name = "winner")
    private Integer winner;

    @Column(name = "p1_tekken_id", length = 32)
    private String p1TekkenId;

    @Column(name = "p1_name")
    private String p1Name;

    @Column(name = "p1_char", length = 64)
    private String p1Char;

    @Column(name = "p1_region", length = 64)
    private String p1Region;

    @Column(name = "p1_dan_rank", length = 128)
    private String p1DanRank;

    @Column(name = "p1_tekken_power")
    private Integer p1TekkenPower;

    @Column(name = "p1_rounds_won")
    private Integer p1RoundsWon;

    @Column(name = "p2_tekken_id", length = 32)
    private String p2TekkenId;

    @Column(name = "p2_name")
    private String p2Name;

    @Column(name = "p2_char", length = 64)
    private String p2Char;

    @Column(name = "p2_region", length = 64)
    private String p2Region;

    @Column(name = "p2_dan_rank", length = 128)
    private String p2DanRank;

    @Column(name = "p2_tekken_power")
    private Integer p2TekkenPower;

    @Column(name = "p2_rounds_won")
    private Integer p2RoundsWon;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_battle_json", columnDefinition = "jsonb")
    private Map<String, Object> rawBattleJson;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MatchEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getExternalMatchKey() {
        return externalMatchKey;
    }

    public Instant getBattleAt() {
        return battleAt;
    }

    public String getBattleType() {
        return battleType;
    }

    public Integer getGameVersion() {
        return gameVersion;
    }

    public Integer getStageId() {
        return stageId;
    }

    public Integer getWinner() {
        return winner;
    }

    public String getP1TekkenId() {
        return p1TekkenId;
    }

    public String getP1Name() {
        return p1Name;
    }

    public String getP1Char() {
        return p1Char;
    }

    public String getP1Region() {
        return p1Region;
    }

    public String getP1DanRank() {
        return p1DanRank;
    }

    public Integer getP1TekkenPower() {
        return p1TekkenPower;
    }

    public Integer getP1RoundsWon() {
        return p1RoundsWon;
    }

    public String getP2TekkenId() {
        return p2TekkenId;
    }

    public String getP2Name() {
        return p2Name;
    }

    public String getP2Char() {
        return p2Char;
    }

    public String getP2Region() {
        return p2Region;
    }

    public String getP2DanRank() {
        return p2DanRank;
    }

    public Integer getP2TekkenPower() {
        return p2TekkenPower;
    }

    public Integer getP2RoundsWon() {
        return p2RoundsWon;
    }

    public Map<String, Object> getRawBattleJson() {
        return rawBattleJson;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
