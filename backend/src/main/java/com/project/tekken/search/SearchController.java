package com.project.tekken.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchSuggestionService searchSuggestionService;
    private final PlayerSearchAutocompleteService playerSearchAutocompleteService;

    public SearchController(
            SearchSuggestionService searchSuggestionService,
            PlayerSearchAutocompleteService playerSearchAutocompleteService
    ) {
        this.searchSuggestionService = searchSuggestionService;
        this.playerSearchAutocompleteService = playerSearchAutocompleteService;
    }

    @GetMapping("/recent")
    public SearchSuggestionResponse recent(@RequestParam(defaultValue = "10") int limit) {
        return searchSuggestionService.recent(limit);
    }

    @GetMapping("/popular")
    public SearchSuggestionResponse popular(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return searchSuggestionService.popular(days, limit);
    }

    @GetMapping("/players")
    public PlayerSearchAutocompleteResponse players(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return playerSearchAutocompleteService.search(q, limit);
    }
}
