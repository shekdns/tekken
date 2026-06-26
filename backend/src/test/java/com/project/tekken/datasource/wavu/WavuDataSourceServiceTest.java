package com.project.tekken.datasource.wavu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.tekken.cache.ApiCacheEntity;
import com.project.tekken.cache.ApiCacheRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class WavuDataSourceServiceTest {

    @Mock
    private WavuDataSourceClient wavuDataSourceClient;

    @Mock
    private ApiCacheRepository apiCacheRepository;

    @InjectMocks
    private WavuDataSourceService wavuDataSourceService;

    @Test
    void parsesPlayerSearchHtml() {
        String html = """
                <div id="search-results">
                  <div class="container">
                    <table>
                      <tbody>
                        <tr>
                          <td>
                            <div><a href="/player/5qAB8redHFEM">LOWHIGH</a></div>
                            <div>5qAB-8red-HFEM</div>
                            <div><svg><title>playstation</title></svg><a>SonKakaoto</a></div>
                          </td>
                          <td><a href="/player/5qAB8redHFEM">5qAB-8red-HFEM</a></td>
                          <td><svg><title>playstation</title></svg><a>SonKakaoto</a></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
                """;

        List<WavuPlayerSearchResult> results = wavuDataSourceService.parsePlayerSearchResults(html);

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.tekkenId()).isEqualTo("5qAB8redHFEM");
            assertThat(result.displayTekkenId()).isEqualTo("5qAB-8red-HFEM");
            assertThat(result.name()).isEqualTo("LOWHIGH");
            assertThat(result.platform()).isEqualTo("playstation");
        });
    }

    @Test
    void returnsEmptyWhenDatasourceIsDisabledWithoutUsingCache() {
        when(wavuDataSourceClient.isEnabled()).thenReturn(false);

        assertThat(wavuDataSourceService.searchPlayers("lowhigh")).isEmpty();

        verify(apiCacheRepository, never()).findById(any());
        verify(wavuDataSourceClient, never()).searchPlayers(any());
    }

    @Test
    void returnsEmptyWhenDatasourceFails() {
        when(wavuDataSourceClient.isEnabled()).thenReturn(true);
        when(apiCacheRepository.findById("wavu:player-search:lowhigh")).thenReturn(Optional.empty());
        when(wavuDataSourceClient.searchPlayers("lowhigh"))
                .thenReturn(ResponseEntity.status(503).body("disabled"));

        assertThat(wavuDataSourceService.searchPlayers("lowhigh")).isEmpty();
    }

    @Test
    void returnsCachedPlayerSearchResultsWithoutCallingWavu() {
        when(wavuDataSourceClient.isEnabled()).thenReturn(true);
        ApiCacheEntity cache = new ApiCacheEntity(
                "wavu:player-search:lowhigh",
                "wavu",
                Map.of("items", List.of(cacheItem(
                        "5qAB8redHFEM",
                        "5qAB-8red-HFEM",
                        "LOWHIGH",
                        "playstation"))),
                Instant.now().plusSeconds(60),
                Instant.now());
        when(apiCacheRepository.findById("wavu:player-search:lowhigh"))
                .thenReturn(Optional.of(cache));

        List<WavuPlayerSearchResult> results = wavuDataSourceService.searchPlayers("LOWHIGH");

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.tekkenId()).isEqualTo("5qAB8redHFEM");
            assertThat(result.displayTekkenId()).isEqualTo("5qAB-8red-HFEM");
            assertThat(result.name()).isEqualTo("LOWHIGH");
            assertThat(result.platform()).isEqualTo("playstation");
        });
        verify(wavuDataSourceClient, never()).searchPlayers(any());
    }

    @Test
    void cachesSuccessfulPlayerSearchResults() {
        String html = """
                <div id="search-results">
                  <table>
                    <tbody>
                      <tr>
                        <td>
                          <div><a href="/player/5qAB8redHFEM">LOWHIGH</a></div>
                          <div>5qAB-8red-HFEM</div>
                          <div><svg><title>playstation</title></svg></div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                """;
        when(wavuDataSourceClient.isEnabled()).thenReturn(true);
        when(apiCacheRepository.findById("wavu:player-search:lowhigh")).thenReturn(Optional.empty());
        when(wavuDataSourceClient.searchPlayers("lowhigh"))
                .thenReturn(ResponseEntity.ok(html));

        List<WavuPlayerSearchResult> results = wavuDataSourceService.searchPlayers("lowhigh");

        assertThat(results).singleElement().satisfies(result ->
                assertThat(result.tekkenId()).isEqualTo("5qAB8redHFEM"));

        ArgumentCaptor<ApiCacheEntity> captor = ArgumentCaptor.forClass(ApiCacheEntity.class);
        verify(apiCacheRepository).save(captor.capture());
        ApiCacheEntity saved = captor.getValue();
        assertThat(saved.getCacheKey()).isEqualTo("wavu:player-search:lowhigh");
        assertThat(saved.getSource()).isEqualTo("wavu");
        assertThat(saved.getResponseJson().get("items")).asList().hasSize(1);
    }

    private Map<String, Object> cacheItem(String tekkenId, String displayTekkenId, String name, String platform) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tekkenId", tekkenId);
        item.put("displayTekkenId", displayTekkenId);
        item.put("name", name);
        item.put("platform", platform);
        return item;
    }
}
