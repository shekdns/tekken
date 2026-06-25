package com.project.tekken.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchSuggestionService searchSuggestionService;

    public SearchController(SearchSuggestionService searchSuggestionService) {
        this.searchSuggestionService = searchSuggestionService;
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
}
