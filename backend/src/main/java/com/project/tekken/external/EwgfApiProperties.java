package com.project.tekken.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ewgf")
public record EwgfApiProperties(
        String baseUrl,
        String apiKey
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
