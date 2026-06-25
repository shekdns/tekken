package com.project.tekken.search;

import java.util.List;

public record SearchSuggestionResponse(
        List<SearchSuggestionItem> items
) {
}
