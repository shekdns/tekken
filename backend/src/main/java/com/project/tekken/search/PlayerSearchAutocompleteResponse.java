package com.project.tekken.search;

import java.util.List;

public record PlayerSearchAutocompleteResponse(
        List<PlayerSearchAutocompleteItem> items
) {
}
