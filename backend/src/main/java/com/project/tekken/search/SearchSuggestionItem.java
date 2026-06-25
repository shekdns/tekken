package com.project.tekken.search;

import java.time.Instant;

public record SearchSuggestionItem(
        String tekkenId,
        String displayTekkenId,
        String query,
        long searchCount,
        Instant lastSearchedAt
) {
}
