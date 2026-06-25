package com.project.tekken.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SearchSuggestionServiceTest {

    @Mock
    private PlayerSearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private SearchSuggestionService searchSuggestionService;

    @Test
    void returnsRecentUniqueSearchesByLatestOrder() {
        Instant now = Instant.now();
        when(searchHistoryRepository.findByTekkenIdIsNotNullOrderBySearchedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(
                        new PlayerSearchHistoryEntity("27tB-4yhF-mfNE", "27tB4yhFmfNE", now),
                        new PlayerSearchHistoryEntity("27tB-4yhF-mfNE", "27tB4yhFmfNE", now.minusSeconds(10)),
                        new PlayerSearchHistoryEntity("abc", "abc", now.minusSeconds(20)),
                        new PlayerSearchHistoryEntity("def", "def", now.minusSeconds(30))));

        SearchSuggestionResponse response = searchSuggestionService.recent(2);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).tekkenId()).isEqualTo("27tB4yhFmfNE");
        assertThat(response.items().get(0).displayTekkenId()).isEqualTo("27tB-4yhF-mfNE");
        assertThat(response.items().get(1).tekkenId()).isEqualTo("abc");
    }

    @Test
    void normalizesRecentLimit() {
        when(searchHistoryRepository.findByTekkenIdIsNotNullOrderBySearchedAtDesc(any(Pageable.class)))
                .thenReturn(List.of());

        searchSuggestionService.recent(0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchHistoryRepository).findByTekkenIdIsNotNullOrderBySearchedAtDesc(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void returnsPopularSearches() {
        Instant now = Instant.now();
        when(searchHistoryRepository.findPopularSearchesSince(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(new Projection("27tB4yhFmfNE", "27tB-4yhF-mfNE", 3, now)));

        SearchSuggestionResponse response = searchSuggestionService.popular(7, 10);

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.tekkenId()).isEqualTo("27tB4yhFmfNE");
            assertThat(item.displayTekkenId()).isEqualTo("27tB-4yhF-mfNE");
            assertThat(item.searchCount()).isEqualTo(3);
            assertThat(item.lastSearchedAt()).isEqualTo(now);
        });
    }

    private record Projection(
            String tekkenId,
            String query,
            long searchCount,
            Instant lastSearchedAt
    ) implements SearchSuggestionProjection {

        @Override
        public String getTekkenId() {
            return tekkenId;
        }

        @Override
        public String getQuery() {
            return query;
        }

        @Override
        public long getSearchCount() {
            return searchCount;
        }

        @Override
        public Instant getLastSearchedAt() {
            return lastSearchedAt;
        }
    }
}
