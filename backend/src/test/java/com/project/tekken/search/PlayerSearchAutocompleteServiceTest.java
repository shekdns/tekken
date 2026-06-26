package com.project.tekken.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.tekken.datasource.wavu.WavuDataSourceService;
import com.project.tekken.datasource.wavu.WavuPlayerSearchResult;
import com.project.tekken.player.PlayerEntity;
import com.project.tekken.player.PlayerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PlayerSearchAutocompleteServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSearchHistoryRepository searchHistoryRepository;

    @Mock
    private WavuDataSourceService wavuDataSourceService;

    @InjectMocks
    private PlayerSearchAutocompleteService autocompleteService;

    @Test
    void returnsEmptyForShortQuery() {
        PlayerSearchAutocompleteResponse response = autocompleteService.search("a", 10);

        assertThat(response.items()).isEmpty();
        verify(playerRepository, never()).findAutocompleteCandidates(any(), any(), any(Pageable.class));
        verify(searchHistoryRepository, never()).findAutocompleteCandidates(any(), any(), any(Pageable.class));
    }

    @Test
    void returnsPlayerCandidatesFirst() {
        Instant now = Instant.now();
        when(playerRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(player("27tB4yhFmfNE", "Dragon Player", "Dragunov", "God of Destruction", now)));
        when(searchHistoryRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(wavuDataSourceService.searchPlayers(any())).thenReturn(List.of());

        PlayerSearchAutocompleteResponse response = autocompleteService.search("dragon", 10);

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.tekkenId()).isEqualTo("27tB4yhFmfNE");
            assertThat(item.name()).isEqualTo("Dragon Player");
            assertThat(item.mainCharacter()).isEqualTo("Dragunov");
            assertThat(item.danRank()).isEqualTo("God of Destruction");
            assertThat(item.source()).isEqualTo("t8lab");
            assertThat(item.lastUpdatedAt()).isEqualTo(now);
        });
    }

    @Test
    void fillsRemainingCandidatesFromSearchHistory() {
        Instant now = Instant.now();
        when(playerRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(player("aaa", "Alpha", "Jin", "Tekken King", now)));
        when(searchHistoryRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(
                        new PlayerSearchHistoryEntity("Alpha", "aaa", now.minusSeconds(5)),
                        new PlayerSearchHistoryEntity("Bravo", "bbb", now.minusSeconds(10))));

        PlayerSearchAutocompleteResponse response = autocompleteService.search("br", 2);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).tekkenId()).isEqualTo("aaa");
        assertThat(response.items().get(0).source()).isEqualTo("t8lab");
        assertThat(response.items().get(1).tekkenId()).isEqualTo("bbb");
        assertThat(response.items().get(1).displayTekkenId()).isEqualTo("Bravo");
        assertThat(response.items().get(1).source()).isEqualTo("search_history");
    }

    @Test
    void normalizesLimitAndQuery() {
        when(playerRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(searchHistoryRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(wavuDataSourceService.searchPlayers(any())).thenReturn(List.of());

        autocompleteService.search("  27tB-4y  ", 99);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> normalizedCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(playerRepository).findAutocompleteCandidates(
                queryCaptor.capture(),
                normalizedCaptor.capture(),
                pageableCaptor.capture());
        assertThat(queryCaptor.getValue()).isEqualTo("27tb-4y");
        assertThat(normalizedCaptor.getValue()).isEqualTo("27tb4y");
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(60);
    }

    @Test
    void fillsRemainingCandidatesFromWavu() {
        when(playerRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(searchHistoryRepository.findAutocompleteCandidates(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(wavuDataSourceService.searchPlayers("lowhigh"))
                .thenReturn(List.of(new WavuPlayerSearchResult(
                        "5qAB8redHFEM",
                        "5qAB-8red-HFEM",
                        "LOWHIGH",
                        "playstation")));

        PlayerSearchAutocompleteResponse response = autocompleteService.search("lowhigh", 10);

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.tekkenId()).isEqualTo("5qAB8redHFEM");
            assertThat(item.displayTekkenId()).isEqualTo("5qAB-8red-HFEM");
            assertThat(item.name()).isEqualTo("LOWHIGH");
            assertThat(item.platform()).isEqualTo("playstation");
            assertThat(item.source()).isEqualTo("wavu");
        });
    }

    private PlayerEntity player(String tekkenId, String name, String character, String rank, Instant now) {
        PlayerEntity player = new PlayerEntity(tekkenId, now);
        player.updateFromProfile(Map.of(
                "name", name,
                "tekkenProwess", 300000,
                "region", "KR",
                "platform", "Steam",
                "mainCharacter", Map.of(character, rank)), now);
        return player;
    }
}
