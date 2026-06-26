package com.project.tekken.datasource.wavu;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wavu")
public record WavuDataSourceProperties(
        String baseUrl,
        boolean enabled
) {
}
