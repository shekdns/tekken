import React, { useEffect, useState } from 'react';
import { Activity, AlertCircle, CheckCircle2, Swords } from 'lucide-react';
import { MatchHistoryPanel } from '../features/match-history/MatchHistoryPanel';
import { PlayerProfileCard } from '../features/player-profile/PlayerProfileCard';
import { PlayerSearchForm } from '../features/player-search/PlayerSearchForm';
import { PlayerStatsPanel } from '../features/player-stats/PlayerStatsPanel';
import { fetchHealth, fetchPlayerMatches, fetchPlayerProfile } from '../shared/api/playerApi';

export function App() {
  const [health, setHealth] = useState(null);
  const [healthError, setHealthError] = useState('');
  const [tekkenId, setTekkenId] = useState('');
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
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

  const searchPlayer = async (event) => {
    event.preventDefault();

    const trimmedId = tekkenId.trim();
    if (!trimmedId) {
      setError('Tekken ID를 입력해 주세요.');
      return;
    }

    setLoading(true);
    setMatchesLoading(false);
    setError('');
    setMatchesError('');
    setProfile(null);
    setMatches([]);

    try {
      setProfile(await fetchPlayerProfile(trimmedId));
      setMatchesLoading(true);

      try {
        setMatches(await fetchPlayerMatches(trimmedId));
      } catch (matchErr) {
        setMatches([]);
        setMatchesError(matchErr.message || '최근 경기 조회 중 오류가 발생했습니다.');
      } finally {
        setMatchesLoading(false);
      }
    } catch (err) {
      setError(err.message || '검색 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHealth();
  }, []);

  return (
    <main className="app-shell">
      <section className="search-section">
        <nav className="topbar" aria-label="서비스 상태">
          <div className="brand">
            <Swords aria-hidden="true" />
            <span>TKNOW LAB</span>
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

        {profile && <PlayerStatsPanel matches={matches} />}

        {profile && (
          <MatchHistoryPanel
            matches={matches}
            loading={matchesLoading}
            error={matchesError}
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
