package com.project.tekken.search;

import java.time.Instant;

public record PlayerSearchAutocompleteItem(
        String tekkenId,
        String displayTekkenId,
        String name,
        String mainCharacter,
        String danRank,
        Integer tekkenProwess,
        String region,
        String platform,
        String source,
        Instant lastUpdatedAt
) {
}
