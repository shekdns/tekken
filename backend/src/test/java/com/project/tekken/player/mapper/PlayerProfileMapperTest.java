package com.project.tekken.player.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.tekken.player.dto.PlayerProfileSummary;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerProfileMapperTest {

    @Test
    void mapsMainCharacterObjectKeyAsCharacterAndValueAsRank() {
        Map<String, Object> profile = Map.of(
                "nickname", "Tekken Player",
                "main_character", Map.of("Dragunov", "God of Destruction"),
                "area", "KR",
                "platform", "Steam",
                "tekken_prowess", "369393",
                "language", "ko",
                "updated_at", "2026-06-23T10:15:30Z");

        PlayerProfileSummary summary = PlayerProfileMapper.toSummary(profile);

        assertThat(summary.name()).isEqualTo("Tekken Player");
        assertThat(summary.mainCharacter()).isEqualTo("Dragunov");
        assertThat(summary.rank()).isEqualTo("God of Destruction");
        assertThat(summary.region()).isEqualTo("KR");
        assertThat(summary.platform()).isEqualTo("Steam");
        assertThat(summary.tekkenProwess()).isEqualTo(369393);
        assertThat(summary.language()).isEqualTo("ko");
        assertThat(summary.lastSeen()).isEqualTo(Instant.parse("2026-06-23T10:15:30Z"));
    }

    @Test
    void prefersExplicitRankAndCharacterFieldsOverMainCharacterFallback() {
        Map<String, Object> profile = Map.of(
                "displayName", "Explicit Player",
                "rankName", "Tekken God Supreme",
                "characterName", "Kazuya",
                "mainCharacter", Map.of("Dragunov", "God of Destruction"));

        PlayerProfileSummary summary = PlayerProfileMapper.toSummary(profile);

        assertThat(summary.name()).isEqualTo("Explicit Player");
        assertThat(summary.rank()).isEqualTo("Tekken God Supreme");
        assertThat(summary.mainCharacter()).isEqualTo("Kazuya");
    }

    @Test
    void mapsNestedProfileRankCharacterAndStatsFields() {
        Map<String, Object> profile = Map.of(
                "profile", Map.of(
                        "name", "Nested Player",
                        "country", "JP",
                        "platform", "PSN",
                        "lastSeen", "2026-06-23T11:00:00Z"),
                "danRank", Map.of("name", "Bushin"),
                "mainCharacter", Map.of("name", "Jin"),
                "stats", Map.of("tekkenProwess", 250000));

        PlayerProfileSummary summary = PlayerProfileMapper.toSummary(profile);

        assertThat(summary.name()).isEqualTo("Nested Player");
        assertThat(summary.rank()).isEqualTo("Bushin");
        assertThat(summary.mainCharacter()).isEqualTo("Jin");
        assertThat(summary.region()).isEqualTo("JP");
        assertThat(summary.platform()).isEqualTo("PSN");
        assertThat(summary.tekkenProwess()).isEqualTo(250000);
        assertThat(summary.lastSeen()).isEqualTo(Instant.parse("2026-06-23T11:00:00Z"));
    }

    @Test
    void ignoresInvalidNumberAndDateValues() {
        Map<String, Object> profile = Map.of(
                "name", "Broken Player",
                "tekken_prowess", "not-a-number",
                "last_seen", "not-a-date");

        PlayerProfileSummary summary = PlayerProfileMapper.toSummary(profile);

        assertThat(summary.name()).isEqualTo("Broken Player");
        assertThat(summary.tekkenProwess()).isNull();
        assertThat(summary.lastSeen()).isNull();
    }
}
