package com.project.tekken.external;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EwgfApiClient {

    private final EwgfApiProperties properties;
    private final RestClient restClient;

    public EwgfApiClient(EwgfApiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ResponseEntity<String> getBattles(String tekkenId) {
        return get("/external/battles/{tekkenId}", tekkenId);
    }

    public ResponseEntity<String> getProfile(String tekkenId) {
        return get("/external/profile/{tekkenId}", tekkenId);
    }

    public ResponseEntity<String> getProfiles(List<String> tekkenIds) {
        if (!properties.hasApiKey()) {
            return missingApiKeyResponse();
        }

        return restClient.post()
                .uri("/external/profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(tekkenIds)
                .exchange((request, response) -> toResponse("/external/profile", response));
    }

    private ResponseEntity<String> get(String path, String tekkenId) {
        if (!properties.hasApiKey()) {
            return missingApiKeyResponse();
        }

        return restClient.get()
                .uri(path, tekkenId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken())
                .exchange((request, response) -> toResponse(path.replace("{tekkenId}", tekkenId), response));
    }

    private String bearerToken() {
        return "Bearer " + properties.apiKey();
    }

    private ResponseEntity<String> toResponse(String path, ClientHttpResponse response)
            throws java.io.IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank() && response.getStatusCode().isError()) {
            body = """
                    {
                      "message": "EWGF API request failed.",
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

    private ResponseEntity<String> missingApiKeyResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "message": "EWGF API key is not configured.",
                          "requiredEnvironmentVariable": "EWGF_API_KEY"
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
