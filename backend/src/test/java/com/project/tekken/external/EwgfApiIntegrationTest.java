package com.project.tekken.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "EWGF_API_KEY", matches = "ewgf_.+")
class EwgfApiIntegrationTest {

    private static final String BASE_URL = "https://api.ewgf.gg";
    private static final String DEFAULT_SAMPLE_TEKKEN_ID = "27tB-4yhF-mfNE";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * API spec: GET /external/battles/{tekkenId}
     * Purpose: Fetch recent battle records for a Tekken player.
     * Auth: Authorization: Bearer {EWGF_API_KEY}
     * Expected response: 200 OK, JSON object with _metadata and data array.
     */
    @Test
    @DisplayName("EWGF battles API returns battle records for a valid Tekken ID")
    void getBattlesReturnsBattleRecordsForValidTekkenId() throws Exception {
        HttpResponse<String> response = sendGet("/external/battles/" + sampleTekkenId(), apiKey());

        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode root = readJson(response);
        assertCommonSuccessEnvelope(root);
        assertThat(root.path("data").isArray()).isTrue();
        assertThat(root.path("data").size()).isGreaterThan(0);

        JsonNode battle = root.path("data").get(0);
        assertThat(battle.path("battle_at").asText()).isNotBlank();
        assertThat(battle.path("battle_type").asText()).isNotBlank();
        assertThat(battle.path("winner").isInt()).isTrue();
        assertThat(battle.path("p1_tekken_id").asText()).isNotBlank();
        assertThat(battle.path("p2_tekken_id").asText()).isNotBlank();
        assertThat(List.of(
                battle.path("p1_tekken_id").asText(),
                battle.path("p2_tekken_id").asText()
        )).contains(normalizeTekkenId(sampleTekkenId()));
    }

    /*
     * API spec: GET /external/profile/{tekkenId}
     * Purpose: Fetch a single Tekken player profile.
     * Auth: Authorization: Bearer {EWGF_API_KEY}
     * Expected response: 200 OK, JSON object with _metadata and data.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "EWGF_ENABLE_PROFILE_TESTS", matches = "true")
    @DisplayName("EWGF single profile API returns profile data for a valid Tekken ID")
    void getProfileReturnsProfileDataForValidTekkenId() throws Exception {
        HttpResponse<String> response = sendGet("/external/profile/" + sampleTekkenId(), apiKey());

        assertThat(response.statusCode())
                .as("EWGF profile API should return 200. If this is 500, the upstream API is failing for the documented sample ID.")
                .isEqualTo(200);

        JsonNode root = readJson(response);
        assertCommonSuccessEnvelope(root);
        assertThat(root.path("data").isMissingNode()).isFalse();
        assertThat(root.path("data").isNull()).isFalse();
    }

    /*
     * API spec: POST /external/profile
     * Purpose: Fetch multiple Tekken player profiles in one request.
     * Auth: Authorization: Bearer {EWGF_API_KEY}
     * Request body: JSON array of Tekken IDs.
     * Expected response: 200 OK, JSON object with _metadata and data.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "EWGF_ENABLE_PROFILE_TESTS", matches = "true")
    @DisplayName("EWGF bulk profile API returns profile data for valid Tekken IDs")
    void postProfileReturnsProfileDataForValidTekkenIds() throws Exception {
        String body = objectMapper.writeValueAsString(List.of(sampleTekkenId()));
        HttpResponse<String> response = sendPost("/external/profile", apiKey(), body);

        assertThat(response.statusCode())
                .as("EWGF bulk profile API should return 200. If this is 500, the upstream API is failing for the documented payload.")
                .isEqualTo(200);

        JsonNode root = readJson(response);
        assertCommonSuccessEnvelope(root);
        assertThat(root.path("data").isMissingNode()).isFalse();
        assertThat(root.path("data").isNull()).isFalse();
    }

    /*
     * API spec: GET /external/battles/{tekkenId}
     * Purpose: Verify the API rejects requests without a bearer token.
     * Auth: none
     * Expected response: 401 Unauthorized.
     */
    @Test
    @DisplayName("EWGF API rejects requests without authorization")
    void getBattlesRejectsMissingAuthorization() throws Exception {
        HttpResponse<String> response = sendGet("/external/battles/" + sampleTekkenId(), null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    /*
     * API spec: GET /external/battles/{tekkenId}
     * Purpose: Verify the API rejects requests with an invalid bearer token.
     * Auth: Authorization: Bearer invalid-token
     * Expected response: 401 Unauthorized.
     */
    @Test
    @DisplayName("EWGF API rejects invalid authorization tokens")
    void getBattlesRejectsInvalidAuthorization() throws Exception {
        HttpResponse<String> response = sendGet("/external/battles/" + sampleTekkenId(), "invalid-token");

        assertThat(response.statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> sendGet(String path, String bearerToken) throws IOException, InterruptedException {
        HttpRequest.Builder request = baseRequest(path).GET();
        if (bearerToken != null) {
            request.header("Authorization", "Bearer " + bearerToken);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String bearerToken, String body)
            throws IOException, InterruptedException {
        HttpRequest request = baseRequest(path)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
    }

    private JsonNode readJson(HttpResponse<String> response) throws IOException {
        assertThat(response.body()).as("response body").isNotBlank();
        return objectMapper.readTree(response.body());
    }

    private void assertCommonSuccessEnvelope(JsonNode root) {
        assertThat(root.path("_metadata").isObject()).isTrue();
        assertThat(root.path("_metadata").path("rate_limit_remaining").isNumber()).isTrue();
        assertThat(root.path("_metadata").path("rate_limit_reset").asText()).isNotBlank();
        assertThat(root.path("_metadata").path("tier").asText()).isNotBlank();
        assertThat(root.path("data").isMissingNode()).isFalse();
    }

    private String apiKey() {
        return System.getenv("EWGF_API_KEY");
    }

    private String sampleTekkenId() {
        return envOrDefault("EWGF_SAMPLE_TEKKEN_ID", DEFAULT_SAMPLE_TEKKEN_ID);
    }

    private String normalizeTekkenId(String tekkenId) {
        return tekkenId.replace("-", "");
    }

    private String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
