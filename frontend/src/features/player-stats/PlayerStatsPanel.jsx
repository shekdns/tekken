import { BarChart3 } from 'lucide-react';
import { calculateMatchStats } from '../../shared/utils/matches';

export function PlayerStatsPanel({ matches }) {
  if (!matches.length) {
    return null;
  }

  const stats = calculateMatchStats(matches);

  return (
    <section className="analysis-panel" aria-label="최근 경기 통계">
      <div className="section-header">
        <div>
          <p className="eyebrow">Recent Analysis</p>
          <h2>최근 100게임 요약</h2>
        </div>
        <span>{stats.winRate}% win rate</span>
      </div>

      <div className="analysis-grid">
        <AnalysisItem label="전적" value={`${stats.wins}승 ${stats.losses}패`} />
        <AnalysisItem label="승률" value={`${stats.winRate}%`} />
        <AnalysisItem label="최근 10게임" value={stats.recent10Record} />
        <AnalysisItem label="최다 사용 캐릭터" value={stats.mostPlayedCharacter} />
      </div>

      <div className="character-usage">
        <div className="mini-heading">
          <BarChart3 aria-hidden="true" />
          <strong>캐릭터 사용률</strong>
        </div>
        <div className="character-list">
          {stats.characterStats.map((character) => (
            <div className="character-row" key={character.character}>
              <div>
                <strong>{character.character}</strong>
                <span>{character.games} games · {character.winRate}%</span>
              </div>
              <div className="usage-bar" aria-hidden="true">
                <span style={{ width: `${Math.min(100, Math.round((character.games / stats.total) * 100))}%` }} />
              </div>
            </div>
          ))}
        </div>
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
