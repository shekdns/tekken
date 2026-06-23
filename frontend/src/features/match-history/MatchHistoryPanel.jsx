import { Activity, AlertCircle, Loader2, Trophy } from 'lucide-react';
import { battleTypeLabel, displayValue, formatDate } from '../../shared/utils/formatters';

export function MatchHistoryPanel({ matches, loading, error }) {
  return (
    <section className="matches-panel" aria-label="최근 경기 목록">
      <div className="section-header">
        <div>
          <p className="eyebrow">Match History</p>
          <h2>최근 경기</h2>
        </div>
        <span>{loading ? '불러오는 중' : `${matches.length} games`}</span>
      </div>

      {error && (
        <div className="message error compact" role="alert">
          <AlertCircle aria-hidden="true" />
          <span>{error}</span>
        </div>
      )}

      {loading && !matches.length && (
        <div className="empty-panel inline">
          <Loader2 aria-hidden="true" className="spin" />
          <div>
            <strong>최근 경기를 불러오고 있습니다.</strong>
            <p>EWGF battles 응답을 T8LAB 매치 형식으로 정리하는 중입니다.</p>
          </div>
        </div>
      )}

      {!loading && !error && !matches.length && (
        <div className="empty-panel inline">
          <Activity aria-hidden="true" />
          <div>
            <strong>표시할 최근 경기가 없습니다.</strong>
            <p>EWGF에서 battles 데이터가 비어 있으면 이 영역이 비어 있습니다.</p>
          </div>
        </div>
      )}

      {!!matches.length && (
        <div className="match-list">
          {matches.slice(0, 12).map((match) => (
            <MatchRow key={match.externalMatchKey || `${match.battleAt}-${match.opponent?.tekkenId}`} match={match} />
          ))}
        </div>
      )}
    </section>
  );
}

function MatchRow({ match }) {
  const isWin = match.result === 'WIN';
  const my = match.my || {};
  const opponent = match.opponent || {};

  return (
    <article className={`match-row ${isWin ? 'win' : 'loss'}`}>
      <div className="result-mark">
        <strong>{isWin ? '승' : match.result === 'LOSS' ? '패' : '-'}</strong>
        <span>{battleTypeLabel(match.battleType)}</span>
      </div>

      <div className="match-main">
        <div className="fighter self">
          <span className="fighter-label">MY</span>
          <strong>{displayValue(my.character)}</strong>
          <p>{displayValue(my.name)}</p>
          <em>{displayValue(my.rank)}</em>
        </div>

        <div className="versus">
          <Trophy aria-hidden="true" />
          <strong>{displayValue(match.roundScore)}</strong>
        </div>

        <div className="fighter">
          <span className="fighter-label">OPP</span>
          <strong>{displayValue(opponent.character)}</strong>
          <p>{displayValue(opponent.name)}</p>
          <em>{displayValue(opponent.rank)}</em>
        </div>
      </div>

      <div className="match-meta">
        <span>{formatDate(match.battleAt)}</span>
      </div>
    </article>
  );
}
