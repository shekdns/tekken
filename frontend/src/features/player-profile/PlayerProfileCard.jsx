import { Clock3, Database, UserRound } from 'lucide-react';
import { displayValue, formatDate } from '../../shared/utils/formatters';
import { profileSummary } from '../../shared/utils/playerProfile';

export function PlayerProfileCard({ profile }) {
  const summary = profileSummary(profile);
  const player = profile?.profile;

  if (!profile || !summary) {
    return null;
  }

  return (
    <section className="result-panel" aria-label="플레이어 검색 결과">
      <div className="result-header">
        <div className="avatar">
          <UserRound aria-hidden="true" />
        </div>
        <div>
          <p className="eyebrow">Player Profile</p>
          <h2>{summary.name}</h2>
          <p>{profile.tekkenId}</p>
        </div>
        <div className={`source-badge ${profile.source === 'cache' ? 'cache' : 'live'}`}>
          <Database aria-hidden="true" />
          <span>{profile.source === 'cache' ? 'Cache' : 'EWGF'}</span>
        </div>
      </div>

      <div className="stat-grid">
        <StatItem label="랭크" value={summary.rank} />
        <StatItem label="주 캐릭터" value={summary.character} />
        <StatItem label="지역" value={summary.region} />
        <StatItem label="Prowess" value={summary.prowess} />
        <StatItem label="플랫폼" value={summary.platform} />
        <StatItem label="조회 시각" value={formatDate(profile.fetchedAt)} icon={<Clock3 aria-hidden="true" />} />
      </div>

      <details className="raw-data">
        <summary>원본 응답 보기</summary>
        <pre>{JSON.stringify(player, null, 2)}</pre>
      </details>
    </section>
  );
}

function StatItem({ label, value, icon }) {
  return (
    <div className="stat-item">
      <span>{label}</span>
      <strong>
        {icon}
        {displayValue(value)}
      </strong>
    </div>
  );
}
