import { BarChart3 } from 'lucide-react';
import { calculateMatchStats } from '../../shared/utils/matches';
import { battleTypeLabel } from '../../shared/utils/formatters';

export function PlayerStatsPanel({ matches, stats }) {
  const nextStats = stats || (matches.length ? calculateMatchStats(matches) : null);

  if (!nextStats) {
    return null;
  }

  return (
    <section className="analysis-panel" aria-label="최근 경기 통계">
      <div className="section-header">
        <div>
          <p className="eyebrow">Recent Analysis</p>
          <h2>최근 100게임 요약</h2>
        </div>
        <span>{nextStats.winRate}% win rate</span>
      </div>

      <div className="analysis-grid">
        <AnalysisItem label="전적" value={`${nextStats.wins}승 ${nextStats.losses}패`} />
        <AnalysisItem label="승률" value={`${nextStats.winRate}%`} />
        <AnalysisItem label="최근 10게임" value={nextStats.recent10Record} />
        <AnalysisItem label="최다 사용 캐릭터" value={nextStats.mostPlayedCharacter} />
      </div>

      <div className="character-usage">
        <div className="mini-heading">
          <BarChart3 aria-hidden="true" />
          <strong>캐릭터 사용률</strong>
        </div>
        <div className="character-list">
          {nextStats.characterStats.map((character) => (
            <div className="character-row" key={character.character}>
              <div>
                <strong>{character.character}</strong>
                <span>{character.games} games · {character.winRate}%</span>
              </div>
              <div className="usage-bar" aria-hidden="true">
                <span style={{ width: `${Math.min(100, Math.round((character.games / nextStats.total) * 100))}%` }} />
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="detail-stats-grid">
        <StatsTable
          title="전투 타입별 승률"
          rows={nextStats.battleTypeStats || []}
          labelKey="battleType"
          labelFormatter={battleTypeLabel}
        />
        <StatsTable
          title="상대 캐릭터별 전적"
          rows={nextStats.opponentCharacterStats || []}
          labelKey="character"
        />
      </div>
    </section>
  );
}

function AnalysisItem({ label, value }) {
  return (
    <div className="analysis-item">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function StatsTable({ title, rows, labelKey, labelFormatter = (value) => value }) {
  if (!rows.length) {
    return null;
  }

  return (
    <div className="detail-stats">
      <div className="mini-heading">
        <BarChart3 aria-hidden="true" />
        <strong>{title}</strong>
      </div>
      <div className="detail-stats-list">
        {rows.slice(0, 6).map((row) => (
          <div className="detail-stat-row" key={row[labelKey]}>
            <strong>{labelFormatter(row[labelKey])}</strong>
            <span>{row.games}게임</span>
            <span>{row.wins}승 {row.losses}패</span>
            <em>{row.winRate}%</em>
          </div>
        ))}
      </div>
    </div>
  );
}
