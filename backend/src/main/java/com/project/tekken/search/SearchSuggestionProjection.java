package com.project.tekken.search;

import java.time.Instant;

public interface SearchSuggestionProjection {

    String getTekkenId();

    String getQuery();

    long getSearchCount();

    Instant getLastSearchedAt();
}
