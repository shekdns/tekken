import { BarChart3 } from 'lucide-react';
import { CharacterPortrait } from '../../shared/components/CharacterPortrait';
import { findCharacterOption } from '../../shared/utils/characters';
import { calculateMatchStats } from '../../shared/utils/matches';
import { localizedBattleTypeLabel } from '../../shared/utils/gameMetadata';

export function PlayerStatsPanel({ matches, stats, characterOptions = [], locale = 'ko', t }) {
  const nextStats = stats || (matches.length ? calculateMatchStats(matches) : null);

  if (!nextStats) {
    return null;
  }

  return (
    <section className="analysis-panel" aria-label={t('stats.ariaLabel')}>
      <div className="section-header">
        <div>
          <p className="eyebrow">{t('stats.eyebrow')}</p>
          <h2>{t('stats.title')}</h2>
        </div>
        <span>{nextStats.winRate}% {t('stats.winRateSuffix')}</span>
      </div>

      <div className="analysis-grid">
        <AnalysisItem label={t('stats.record')} value={formatRecord(nextStats.wins, nextStats.losses, t)} />
        <AnalysisItem label={t('stats.winRate')} value={`${nextStats.winRate}%`} tone="primary" />
        <AnalysisItem label={t('stats.recent10')} value={formatRecent10(nextStats, t)} />
        <AnalysisItem label={t('stats.mostPlayedCharacter')} value={nextStats.mostPlayedCharacter} />
        <AnalysisItem label={t('stats.currentStreak')} value={formatCurrentStreak(nextStats.streakStats, t)} tone={nextStats.streakStats?.currentType === 'WIN' ? 'positive' : nextStats.streakStats?.currentType === 'LOSS' ? 'negative' : ''} />
        <AnalysisItem label={t('stats.longestWinStreak')} value={formatStreakCount(nextStats.streakStats?.longestWin, t('stats.winStreak'))} />
        <AnalysisItem label={t('stats.longestLossStreak')} value={formatStreakCount(nextStats.streakStats?.longestLoss, t('stats.lossStreak'))} />
        <AnalysisItem label={t('stats.activeDays')} value={formatActiveDays(nextStats.activityStats?.activeDays, t)} />
      </div>

      <div className="character-usage">
        <div className="mini-heading">
          <BarChart3 aria-hidden="true" />
          <strong>{t('stats.characterUsage')}</strong>
        </div>
        <div className="character-list">
          {nextStats.characterStats.map((character) => (
            <div className="character-row" key={character.character}>
              <div className="character-stat-label">
                <CharacterPortrait
                  character={findCharacterOption(characterOptions, character.character)}
                  label={character.character}
                  size="sm"
                />
                <div>
                  <strong>{character.character}</strong>
                  <span>{character.games} {t('stats.games')} · {character.winRate}%</span>
                </div>
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
          title={t('stats.battleTypeStats')}
          rows={nextStats.battleTypeStats || []}
          labelKey="battleType"
          labelFormatter={(value) => localizedBattleTypeLabel(value, locale)}
          t={t}
        />
        <StatsTable
          title={t('stats.opponentCharacterStats')}
          rows={nextStats.opponentCharacterStats || []}
          labelKey="character"
          characterOptions={characterOptions}
          t={t}
        />
      </div>
    </section>
  );
}

function AnalysisItem({ label, value, tone }) {
  return (
    <div className={`analysis-item ${tone || ''}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function StatsTable({ title, rows, labelKey, labelFormatter = (value) => value, characterOptions = [], t }) {
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
            <div className="detail-stat-label">
              {labelKey === 'character' && (
                <CharacterPortrait
                  character={findCharacterOption(characterOptions, row[labelKey])}
                  label={row[labelKey]}
                  size="xs"
                />
              )}
              <strong>{labelFormatter(row[labelKey])}</strong>
            </div>
            <span>{row.games}{t('stats.games')}</span>
            <span>{formatRecord(row.wins, row.losses, t)}</span>
            <em>{row.winRate}%</em>
          </div>
        ))}
      </div>
    </div>
  );
}

function formatRecord(wins, losses, t) {
  return `${wins}${t('stats.wins')} ${losses}${t('stats.losses')}`;
}

function formatRecent10(stats, t) {
  if (stats.recent10Wins !== undefined && stats.recent10Losses !== undefined) {
    return formatRecord(stats.recent10Wins, stats.recent10Losses, t);
  }
  return stats.recent10Record;
}

function formatCurrentStreak(streakStats, t) {
  if (!streakStats || !streakStats.currentCount || streakStats.currentType === '-') {
    return t('stats.noStreak');
  }
  if (streakStats.currentType === 'WIN') {
    return formatStreakCount(streakStats.currentCount, t('stats.winStreak'));
  }
  if (streakStats.currentType === 'LOSS') {
    return formatStreakCount(streakStats.currentCount, t('stats.lossStreak'));
  }
  return t('stats.noStreak');
}

function formatStreakCount(value, label) {
  if (!value) {
    return '-';
  }
  return `${value} ${label}`;
}

function formatActiveDays(value, t) {
  if (!value) {
    return '-';
  }
  return `${value}${t('stats.daysUnit')}`;
}
