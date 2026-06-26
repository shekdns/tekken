import React, { useEffect, useState } from 'react';
import { Activity, AlertCircle, CheckCircle2, Swords } from 'lucide-react';
import { PlayerLeaderboardPanel } from '../features/leaderboard/PlayerLeaderboardPanel';
import { MatchHistoryPanel } from '../features/match-history/MatchHistoryPanel';
import { DEFAULT_PLAYER_FILTERS, PlayerFiltersPanel, toApiFilters } from '../features/player-filters/PlayerFiltersPanel';
import { PlayerProfileCard } from '../features/player-profile/PlayerProfileCard';
import { PlayerSearchForm } from '../features/player-search/PlayerSearchForm';
import { PlayerStatsPanel } from '../features/player-stats/PlayerStatsPanel';
import {
  fetchCharacterOptions,
  fetchHealth,
  fetchPlayerAutocomplete,
  fetchPlayerLeaderboard,
  fetchPlayerMatches,
  fetchPlayerProfile,
  fetchPlayerStats,
  fetchPopularSearches,
  fetchRecentSearches,
} from '../shared/api/playerApi';
import { createTranslator, SUPPORTED_LOCALES, translateApiError } from '../shared/i18n/messages';
import { pushHomePath, pushPlayerPath, tekkenIdFromPath } from '../shared/utils/routing';

export function App() {
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [tekkenId, setTekkenId] = useState('');
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [matchPage, setMatchPage] = useState({ total: 0, nextOffset: null, hasMore: false });
  const [locale, setLocale] = useState('ko');
  const [filters, setFilters] = useState(DEFAULT_PLAYER_FILTERS);
  const [characterOptions, setCharacterOptions] = useState([]);
  const [recentSearches, setRecentSearches] = useState([]);
  const [popularSearches, setPopularSearches] = useState([]);
  const [autocompleteItems, setAutocompleteItems] = useState([]);
  const [leaderboardSort, setLeaderboardSort] = useState('prowess');
  const [leaderboardItems, setLeaderboardItems] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [matchesLoading, setMatchesLoading] = useState(false);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const [autocompleteLoading, setAutocompleteLoading] = useState(false);
  const [leaderboardLoading, setLeaderboardLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [matchesError, setMatchesError] = useState('');
  const [leaderboardError, setLeaderboardError] = useState('');
  const [error, setError] = useState('');
  const t = createTranslator(locale);

  const loadHealth = async () => {
    try {
      setHealth(await fetchHealth());
      setHealthError('');
    } catch {
      setHealth(null);
      setHealthError('');
    }
  };

  const loadCharacterOptions = async () => {
    try {
      setCharacterOptions(await fetchCharacterOptions());
    } catch {
      setCharacterOptions([]);
    }
  };

  const loadSearchSuggestions = async () => {
    setSuggestionsLoading(true);
    try {
      const [recent, popular] = await Promise.all([
        fetchRecentSearches(8),
        fetchPopularSearches({ days: 7, limit: 8 }),
      ]);
      setRecentSearches(recent);
      setPopularSearches(popular);
    } catch {
      setRecentSearches([]);
      setPopularSearches([]);
    } finally {
      setSuggestionsLoading(false);
    }
  };

  const loadLeaderboard = async (sort = leaderboardSort) => {
    setLeaderboardLoading(true);
    setLeaderboardError('');
    try {
      const response = await fetchPlayerLeaderboard({ sort, limit: 10 });
      setLeaderboardItems(response.items);
    } catch (leaderboardErr) {
      setLeaderboardItems([]);
      setLeaderboardError(translateApiError(locale, leaderboardErr, 'leaderboard.error'));
    } finally {
      setLeaderboardLoading(false);
    }
  };

  const resetPlayerData = () => {
    setMatches([]);
    setMatchPage({ total: 0, nextOffset: null, hasMore: false });
    setStats(null);
    setMatchesError('');
  };

  const loadPlayerData = async (playerId, nextFilters, { offset = 0, append = false, refresh = false } = {}) => {
    const apiFilters = toApiFilters(nextFilters);

    setMatchesLoading(true);
    setMatchesError('');

    try {
      const matchResponse = await fetchPlayerMatches(playerId, {
        ...apiFilters,
        offset,
        limit: 12,
        refresh,
      });

      setMatches((current) => (append ? [...current, ...matchResponse.matches] : matchResponse.matches));
      setMatchPage({
        total: matchResponse.total || matchResponse.matches.length,
        nextOffset: matchResponse.nextOffset,
        hasMore: Boolean(matchResponse.hasMore),
      });

      if (!append) {
        try {
          setStats(await fetchPlayerStats(playerId, apiFilters));
        } catch {
          setStats(null);
        }
      }
    } catch (matchErr) {
      if (!append) {
        setMatches([]);
        setMatchPage({ total: 0, nextOffset: null, hasMore: false });
        setStats(null);
      }
      setMatchesError(translateApiError(locale, matchErr, 'errors.matches'));
    } finally {
      setMatchesLoading(false);
    }
  };

  const loadPlayer = async (nextTekkenId, { updatePath = false } = {}) => {
    const trimmedId = nextTekkenId.trim();
    if (!trimmedId) {
      setError(t('search.emptyError'));
      return;
    }

    setLoading(true);
    setMatchesLoading(false);
    setError('');
    resetPlayerData();
    setProfile(null);
    setFilters(DEFAULT_PLAYER_FILTERS);

    try {
      if (updatePath) {
        pushPlayerPath(trimmedId);
      }
      setTekkenId(trimmedId);
      setProfile(await fetchPlayerProfile(trimmedId));
      await loadPlayerData(trimmedId, DEFAULT_PLAYER_FILTERS);
      await loadSearchSuggestions();
    } catch (err) {
      setError(translateApiError(locale, err, 'search.genericError'));
    } finally {
      setLoading(false);
    }
  };

  const searchPlayer = async (event) => {
    event.preventDefault();
    const selectedCandidate = autocompleteItems.length > 0
      ? matchingAutocompleteItem(tekkenId, autocompleteItems) || autocompleteItems[0]
      : null;
    const nextTekkenId = selectedCandidate?.tekkenId || tekkenId;
    setAutocompleteItems([]);
    await loadPlayer(nextTekkenId, { updatePath: true });
  };

  const resetToHome = () => {
    setTekkenId('');
    setAutocompleteItems([]);
    setProfile(null);
    resetPlayerData();
    setFilters(DEFAULT_PLAYER_FILTERS);
    setError('');
    pushHomePath();
  };

  useEffect(() => {
    loadHealth();
    loadCharacterOptions();
    loadSearchSuggestions();
  }, []);

  useEffect(() => {
    loadLeaderboard(leaderboardSort);
  }, [leaderboardSort]);

  useEffect(() => {
    const normalizedQuery = tekkenId.trim();
    if (normalizedQuery.length < 2) {
      setAutocompleteItems([]);
      setAutocompleteLoading(false);
      return undefined;
    }

    let active = true;
    setAutocompleteLoading(true);

    const timeoutId = window.setTimeout(async () => {
      try {
        const items = await fetchPlayerAutocomplete(normalizedQuery, 8);
        if (active) {
          setAutocompleteItems(items);
        }
      } catch {
        if (active) {
          setAutocompleteItems([]);
        }
      } finally {
        if (active) {
          setAutocompleteLoading(false);
        }
      }
    }, 300);

    return () => {
      active = false;
      window.clearTimeout(timeoutId);
    };
  }, [tekkenId]);

  useEffect(() => {
    const loadFromPath = () => {
      const pathTekkenId = tekkenIdFromPath();
      if (pathTekkenId) {
        loadPlayer(pathTekkenId);
      } else {
        setTekkenId('');
        setProfile(null);
        resetPlayerData();
        setFilters(DEFAULT_PLAYER_FILTERS);
        setError('');
      }
    };

    loadFromPath();
    window.addEventListener('popstate', loadFromPath);
    return () => window.removeEventListener('popstate', loadFromPath);
  }, []);

  const loadMoreMatches = async () => {
    if (!tekkenId || !matchPage.hasMore || matchPage.nextOffset === null) {
      return;
    }

    setMatchesLoading(true);
    setMatchesError('');

    try {
      await loadPlayerData(tekkenId, filters, {
        offset: matchPage.nextOffset,
        append: true,
      });
    } catch (matchErr) {
      setMatchesError(translateApiError(locale, matchErr, 'errors.matches'));
    }
  };

  const applyFilters = async () => {
    if (!tekkenId || matchesLoading) {
      return;
    }

    resetPlayerData();
    await loadPlayerData(tekkenId, filters);
  };

  const resetFilters = async () => {
    if (!tekkenId || matchesLoading) {
      return;
    }

    setFilters(DEFAULT_PLAYER_FILTERS);
    resetPlayerData();
    await loadPlayerData(tekkenId, DEFAULT_PLAYER_FILTERS);
  };

  const refreshPlayerData = async () => {
    if (!tekkenId || matchesLoading || refreshing) {
      return;
    }

    setRefreshing(true);
    setError('');
    setMatchesError('');

    try {
      setProfile(await fetchPlayerProfile(tekkenId, { refresh: true }));
      await loadPlayerData(tekkenId, filters, { refresh: true });
    } catch (err) {
      setMatchesError(translateApiError(locale, err, 'errors.refresh'));
    } finally {
      setRefreshing(false);
    }
  };

  const selectSuggestedPlayer = async (suggestedTekkenId) => {
    setAutocompleteItems([]);
    await loadPlayer(suggestedTekkenId, { updatePath: true });
  };

  const changeLeaderboardSort = (nextSort) => {
    setLeaderboardSort(nextSort);
  };

  return (
    <main className="app-shell">
      <section className="search-section">
        <nav className="topbar" aria-label={t('app.serviceStatus')}>
          <div className="brand">
            <Swords aria-hidden="true" />
            <button type="button" onClick={resetToHome}>T8LAB</button>
          </div>
          <div className="topbar-actions">
            <label className="language-select">
              <span>{t('app.language')}</span>
              <select value={locale} onChange={(event) => setLocale(event.target.value)}>
                {SUPPORTED_LOCALES.map((option) => (
                  <option key={option.code} value={option.code}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
            <div className={`server-pill ${health ? 'online' : 'offline'}`}>
              {health ? <CheckCircle2 aria-hidden="true" /> : <AlertCircle aria-hidden="true" />}
              <span>{health ? t('app.backendOnline') : healthError || t('app.backendOffline')}</span>
            </div>
          </div>
        </nav>

        <div className="search-layout">
          <div className="search-copy">
            <p className="eyebrow">{t('search.eyebrow')}</p>
            <h1>{t('search.title')}</h1>
            <p className="lead">{t('search.description')}</p>
          </div>

          <PlayerSearchForm
            tekkenId={tekkenId}
            loading={loading}
            suggestionsLoading={suggestionsLoading}
            autocompleteLoading={autocompleteLoading}
            recentSearches={recentSearches}
            popularSearches={popularSearches}
            autocompleteItems={autocompleteItems}
            t={t}
            onChange={setTekkenId}
            onSubmit={searchPlayer}
            onSuggestionSelect={selectSuggestedPlayer}
          />
        </div>

        {error && (
          <div className="message error" role="alert">
            <AlertCircle aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        {profile && (
          <div className="player-dashboard">
            <aside className="player-sidebar" aria-label={t('app.sidebarLabel')}>
              <PlayerProfileCard
                profile={profile}
                characterOptions={characterOptions}
                locale={locale}
                t={t}
                refreshing={refreshing}
                refreshDisabled={matchesLoading || refreshing}
                onRefresh={refreshPlayerData}
              />

              <PlayerFiltersPanel
                filters={filters}
                characterOptions={characterOptions}
                locale={locale}
                t={t}
                loading={matchesLoading}
                onChange={setFilters}
                onSubmit={applyFilters}
                onReset={resetFilters}
              />
            </aside>

            <div className="player-main" aria-label={t('app.mainLabel')}>
              <PlayerStatsPanel
                matches={matches}
                stats={stats}
                characterOptions={characterOptions}
                locale={locale}
                t={t}
              />

              <MatchHistoryPanel
                matches={matches}
                characterOptions={characterOptions}
                locale={locale}
                t={t}
                loading={matchesLoading || refreshing}
                refreshing={refreshing}
                error={matchesError}
                total={matchPage.total}
                hasMore={matchPage.hasMore}
                onLoadMore={loadMoreMatches}
              />
            </div>
          </div>
        )}

        {!profile && !error && (
          <section className="empty-panel" aria-label={t('app.emptyTitle')}>
            <Activity aria-hidden="true" />
            <div>
              <strong>{t('app.emptyTitle')}</strong>
              <p>{t('app.emptyDescription')}</p>
            </div>
          </section>
        )}

        <PlayerLeaderboardPanel
          items={leaderboardItems}
          sort={leaderboardSort}
          loading={leaderboardLoading}
          error={leaderboardError}
          characterOptions={characterOptions}
          locale={locale}
          t={t}
          onSortChange={changeLeaderboardSort}
          onPlayerSelect={selectSuggestedPlayer}
        />
      </section>
    </main>
  );
}

function matchingAutocompleteItem(query, items) {
  const normalizedQuery = normalizeSearchText(query);
  if (!normalizedQuery) {
    return null;
  }

  return items.find((item) => {
    const values = [
      item.tekkenId,
      item.displayTekkenId,
      item.name,
    ];
    return values.some((value) => normalizeSearchText(value) === normalizedQuery);
  }) || null;
}

function normalizeSearchText(value) {
  return String(value || '')
    .replaceAll('-', '')
    .trim()
    .toLowerCase();
}
