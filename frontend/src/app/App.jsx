import React, { useEffect, useState } from 'react';
import { Activity, AlertCircle, CheckCircle2, Swords } from 'lucide-react';
import { MatchHistoryPanel } from '../features/match-history/MatchHistoryPanel';
import { DEFAULT_PLAYER_FILTERS, PlayerFiltersPanel, toApiFilters } from '../features/player-filters/PlayerFiltersPanel';
import { PlayerProfileCard } from '../features/player-profile/PlayerProfileCard';
import { PlayerSearchForm } from '../features/player-search/PlayerSearchForm';
import { PlayerStatsPanel } from '../features/player-stats/PlayerStatsPanel';
import { fetchHealth, fetchPlayerMatches, fetchPlayerProfile, fetchPlayerStats } from '../shared/api/playerApi';
import { pushHomePath, pushPlayerPath, tekkenIdFromPath } from '../shared/utils/routing';

export function App() {
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [tekkenId, setTekkenId] = useState('');
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [matchPage, setMatchPage] = useState({ total: 0, nextOffset: null, hasMore: false });
  const [filters, setFilters] = useState(DEFAULT_PLAYER_FILTERS);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [matchesLoading, setMatchesLoading] = useState(false);
  const [matchesError, setMatchesError] = useState('');
  const [error, setError] = useState('');

  const loadHealth = async () => {
    try {
      setHealth(await fetchHealth());
      setHealthError('');
    } catch {
      setHealth(null);
      setHealthError('백엔드 연결 대기 중');
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
      setMatchesError(matchErr.message || '최근 경기 조회 중 오류가 발생했습니다.');
    } finally {
      setMatchesLoading(false);
    }
  };

  const loadPlayer = async (nextTekkenId, { updatePath = false } = {}) => {
    const trimmedId = nextTekkenId.trim();
    if (!trimmedId) {
      setError('Tekken ID를 입력해 주세요.');
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
    } catch (err) {
      setError(err.message || '검색 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const searchPlayer = async (event) => {
    event.preventDefault();
    await loadPlayer(tekkenId, { updatePath: true });
  };

  const resetToHome = () => {
    setTekkenId('');
    setProfile(null);
    resetPlayerData();
    setFilters(DEFAULT_PLAYER_FILTERS);
    setError('');
    pushHomePath();
  };

  useEffect(() => {
    loadHealth();
  }, []);

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
      setMatchesError(matchErr.message || '최근 경기 조회 중 오류가 발생했습니다.');
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

  const refreshMatches = async () => {
    if (!tekkenId || matchesLoading) {
      return;
    }

    await loadPlayerData(tekkenId, filters, { refresh: true });
  };

  return (
    <main className="app-shell">
      <section className="search-section">
        <nav className="topbar" aria-label="서비스 상태">
          <div className="brand">
            <Swords aria-hidden="true" />
            <button type="button" onClick={resetToHome}>T8LAB</button>
          </div>
          <div className={`server-pill ${health ? 'online' : 'offline'}`}>
            {health ? <CheckCircle2 aria-hidden="true" /> : <AlertCircle aria-hidden="true" />}
            <span>{health ? 'Backend online' : healthError || 'Backend offline'}</span>
          </div>
        </nav>

        <div className="search-layout">
          <div className="search-copy">
            <p className="eyebrow">Tekken 8 Player Search</p>
            <h1>플레이어 전적을 빠르게 확인하세요</h1>
            <p className="lead">
              Tekken ID 기준으로 EWGF 데이터를 조회하고, 이후 매치 히스토리와 캐릭터 통계까지 확장할 첫 검색 화면입니다.
            </p>
          </div>

          <PlayerSearchForm
            tekkenId={tekkenId}
            loading={loading}
            onChange={setTekkenId}
            onSubmit={searchPlayer}
          />
        </div>

        {error && (
          <div className="message error" role="alert">
            <AlertCircle aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        <PlayerProfileCard profile={profile} />

        {profile && (
          <PlayerFiltersPanel
            filters={filters}
            loading={matchesLoading}
            onChange={setFilters}
            onSubmit={applyFilters}
            onReset={resetFilters}
          />
        )}

        {profile && <PlayerStatsPanel matches={matches} stats={stats} />}

        {profile && (
          <MatchHistoryPanel
            matches={matches}
            loading={matchesLoading}
            error={matchesError}
            total={matchPage.total}
            hasMore={matchPage.hasMore}
            onRefresh={refreshMatches}
            onLoadMore={loadMoreMatches}
          />
        )}

        {!profile && !error && (
          <section className="empty-panel" aria-label="검색 전 상태">
            <Activity aria-hidden="true" />
            <div>
              <strong>검색 결과가 여기에 표시됩니다.</strong>
              <p>백엔드와 DB가 실행된 상태에서 Tekken ID를 입력하면 프로필을 불러옵니다.</p>
            </div>
          </section>
        )}
      </section>
    </main>
  );
}
