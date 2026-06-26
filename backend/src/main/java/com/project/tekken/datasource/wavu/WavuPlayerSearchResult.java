package com.project.tekken.datasource.wavu;

public record WavuPlayerSearchResult(
        String tekkenId,
        String displayTekkenId,
        String name,
        String platform
) {
}
