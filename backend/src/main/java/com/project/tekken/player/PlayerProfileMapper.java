package com.project.tekken.player;

import java.time.Instant;
import java.util.List;
import java.util.Map;

final class PlayerProfileMapper {

    private PlayerProfileMapper() {
    }

    static PlayerProfileSummary toSummary(Map<String, Object> profile) {
        Map<String, Object> mainCharacter = object(
                profile,
                "main_character",
                "mainCharacter",
                "favorite_character",
                "favoriteCharacter",
                "most_played_character",
                "mostPlayedCharacter",
                "character");

        return new PlayerProfileSummary(
                text(profile,
                        "name",
                        "nickname",
                        "player_name",
                        "playerName",
                        "display_name",
                        "displayName",
                        "steam_name",
                        "steamName",
                        "profile.name",
                        "profile.nickname",
                        "player.name",
                        "player.nickname"),
                firstText(
                        text(profile,
                                "rank",
                                "rank_name",
                                "rankName",
                                "dan_rank",
                                "danRank",
                                "highest_rank",
                                "highestRank",
                                "rank.name",
                                "rank.title",
                                "dan.name",
                                "danRank.name",
                                "rating.rank",
                                "stats.rank"),
                        mainCharacterRank(mainCharacter)),
                firstText(
                        text(profile,
                                "main_character_name",
                                "mainCharacterName",
                                "favorite_character_name",
                                "favoriteCharacterName",
                                "most_played_character_name",
                                "mostPlayedCharacterName",
                                "character_name",
                                "characterName",
                                "main_character.name",
                                "mainCharacter.name",
                                "favorite_character.name",
                                "favoriteCharacter.name",
                                "most_played_character.name",
                                "mostPlayedCharacter.name",
                                "character.name"),
                        mainCharacterName(mainCharacter)),
                text(profile,
                        "region",
                        "country",
                        "area",
                        "profile.region",
                        "profile.country",
                        "player.region",
                        "player.country"),
                text(profile,
                        "platform",
                        "network",
                        "provider",
                        "platform.name",
                        "account.platform",
                        "profile.platform",
                        "player.platform"),
                integer(profile,
                        "tekken_prowess",
                        "tekkenProwess",
                        "prowess",
                        "power",
                        "tekken_power",
                        "tekkenPower",
                        "stats.tekkenProwess",
                        "stats.prowess",
                        "profile.tekkenProwess"),
                text(profile,
                        "language",
                        "profile.language",
                        "player.language"),
                instant(profile,
                        "last_seen",
                        "lastSeen",
                        "updated_at",
                        "updatedAt",
                        "profile.lastSeen"));
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String mainCharacterName(Map<String, Object> mainCharacter) {
        if (mainCharacter == null || mainCharacter.isEmpty()) {
            return null;
        }

        String namedValue = text(mainCharacter,
                "name",
                "character",
                "character_name",
                "characterName");
        if (namedValue != null) {
            return namedValue;
        }

        if (mainCharacter.size() == 1) {
            return mainCharacter.keySet().iterator().next();
        }

        return null;
    }

    private static String mainCharacterRank(Map<String, Object> mainCharacter) {
        if (mainCharacter == null || mainCharacter.isEmpty()) {
            return null;
        }

        String rankedValue = text(mainCharacter,
                "rank",
                "rank_name",
                "rankName",
                        "dan_rank",
                        "danRank",
                        "highest_rank",
                        "highestRank",
                        "rank.name",
                        "rank.title",
                        "dan.name",
                "danRank.name",
                "rating.rank",
                "stats.rank");
        if (rankedValue != null) {
            return rankedValue;
        }

        if (mainCharacter.size() == 1) {
            Object value = mainCharacter.values().iterator().next();
            return value == null ? null : String.valueOf(value);
        }

        return null;
    }

    static String text(Map<String, Object> source, String... paths) {
        Object value = first(source, paths);
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Object nestedName = firstMapValue(map, List.of("name", "title", "label", "value", "id"));
            return nestedName == null ? null : String.valueOf(nestedName);
        }
        return String.valueOf(value);
    }

    static Integer integer(Map<String, Object> source, String... paths) {
        Object value = first(source, paths);
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

    static Instant instant(Map<String, Object> source, String... paths) {
        Object value = first(source, paths);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> object(Map<String, Object> source, String... paths) {
        Object value = first(source, paths);
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
    }

    private static Object first(Map<String, Object> source, String... paths) {
        for (String path : paths) {
            Object value = valueAt(source, path);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object valueAt(Map<String, Object> source, String path) {
        Object current = source;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = firstMapValue(map, List.of(part));
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static Object firstMapValue(Map<?, ?> source, List<String> keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
