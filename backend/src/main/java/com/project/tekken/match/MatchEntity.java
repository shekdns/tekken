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

    public MatchEntity(String externalMatchKey, Instant now) {
        this.externalMatchKey = externalMatchKey;
        this.createdAt = now;
        this.fetchedAt = now;
    }

    public void updateFromBattle(Map<String, Object> battle, Instant now) {
        this.battleAt = instant(battle, "battle_at", "battleAt");
        this.battleType = text(battle, "battle_type", "battleType");
        this.gameVersion = integer(battle, "game_version", "gameVersion");
        this.stageId = integer(battle, "stage_id", "stageId");
        this.winner = integer(battle, "winner");
        this.p1TekkenId = normalizeTekkenId(text(battle, "p1_tekken_id", "p1TekkenId"));
        this.p1Name = text(battle, "p1_name", "p1Name");
        this.p1Char = text(battle, "p1_char", "p1Char", "p1_character", "p1Character");
        this.p1Region = text(battle, "p1_region", "p1Region");
        this.p1DanRank = text(battle, "p1_dan_rank", "p1DanRank");
        this.p1TekkenPower = integer(battle, "p1_tekken_power", "p1TekkenPower", "p1_tekken_prowess");
        this.p1RoundsWon = integer(battle, "p1_rounds_won", "p1RoundsWon");
        this.p2TekkenId = normalizeTekkenId(text(battle, "p2_tekken_id", "p2TekkenId"));
        this.p2Name = text(battle, "p2_name", "p2Name");
        this.p2Char = text(battle, "p2_char", "p2Char", "p2_character", "p2Character");
        this.p2Region = text(battle, "p2_region", "p2Region");
        this.p2DanRank = text(battle, "p2_dan_rank", "p2DanRank");
        this.p2TekkenPower = integer(battle, "p2_tekken_power", "p2TekkenPower", "p2_tekken_prowess");
        this.p2RoundsWon = integer(battle, "p2_rounds_won", "p2RoundsWon");
        this.rawBattleJson = battle;
        this.fetchedAt = now;
    }

    public boolean hasBattleAt() {
        return battleAt != null;
    }

    public static String externalKey(Map<String, Object> battle) {
        String providedKey = text(battle, "id", "battle_id", "match_id", "external_match_key");
        if (providedKey != null && !providedKey.isBlank()) {
            return providedKey;
        }
        return String.join(":",
                textOrEmpty(battle, "battle_at", "battleAt"),
                textOrEmpty(battle, "p1_tekken_id", "p1TekkenId"),
                textOrEmpty(battle, "p2_tekken_id", "p2TekkenId"),
                textOrEmpty(battle, "p1_char", "p1Char", "p1_character", "p1Character"),
                textOrEmpty(battle, "p2_char", "p2Char", "p2_character", "p2Character"),
                textOrEmpty(battle, "winner"));
    }

    private static String textOrEmpty(Map<String, Object> source, String... keys) {
        String value = text(source, keys);
        return value == null ? "" : value;
    }

    private static String normalizeTekkenId(String tekkenId) {
        return tekkenId == null ? null : tekkenId.replace("-", "");
    }

    private static String text(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        return value == null ? null : String.valueOf(value);
    }

    private static Integer integer(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Instant instant(Map<String, Object> source, String... keys) {
        Object value = first(source, keys);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Object first(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
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
