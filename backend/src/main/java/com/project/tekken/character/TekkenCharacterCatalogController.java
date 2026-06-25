package com.project.tekken.character;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/characters")
public class TekkenCharacterCatalogController {

    private final TekkenCharacterCatalogService characterCatalogService;

    public TekkenCharacterCatalogController(TekkenCharacterCatalogService characterCatalogService) {
        this.characterCatalogService = characterCatalogService;
    }

    @GetMapping("/options")
    public TekkenCharacterOptionsResponse getCharacterOptions() {
        return characterCatalogService.getCharacterOptions();
    }
}
