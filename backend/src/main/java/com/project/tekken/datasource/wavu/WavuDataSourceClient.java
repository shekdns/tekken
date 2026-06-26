package com.project.tekken.datasource.wavu;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WavuDataSourceClient {

    private final WavuDataSourceProperties properties;
    private final RestClient restClient;

    public WavuDataSourceClient(WavuDataSourceProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ResponseEntity<String> get(String path) {
        if (!properties.enabled()) {
            return disabledResponse();
        }

        return restClient.get()
                .uri(path)
                .exchange((request, response) -> toResponse(path, response));
    }

    public ResponseEntity<String> searchPlayers(String query) {
        if (!properties.enabled()) {
            return disabledResponse();
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/player/search")
                        .queryParam("q", "{query}")
                        .build(query))
                .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.ALL)
                .exchange((request, response) -> toResponse("/player/search?q=" + query, response));
    }

    public boolean isEnabled() {
        return properties.enabled();
    }

    private ResponseEntity<String> toResponse(String path, ClientHttpResponse response)
            throws java.io.IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank() && response.getStatusCode().isError()) {
            body = """
                    {
                      "message": "Wavu datasource request failed.",
                      "upstreamStatus": %d,
                      "path": "%s"
                    }
                    """.formatted(response.getStatusCode().value(), path);
        }

        return ResponseEntity
                .status(response.getStatusCode())
                .headers(filterResponseHeaders(response.getHeaders()))
                .body(body);
    }

    private ResponseEntity<String> disabledResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "message": "Wavu datasource is not enabled.",
                          "requiredConfiguration": "wavu.enabled=true"
                        }
                        """);
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders upstreamHeaders) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = upstreamHeaders.getContentType();
        if (contentType != null) {
            headers.setContentType(contentType);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }
}
