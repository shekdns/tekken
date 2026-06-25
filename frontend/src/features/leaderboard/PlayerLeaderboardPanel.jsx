import { Loader2, Search, Trophy } from 'lucide-react';
import { CharacterPortrait } from '../../shared/components/CharacterPortrait';
import { displayValue, formatDateByLocale } from '../../shared/utils/formatters';
import { findCharacterOption } from '../../shared/utils/characters';

const SORT_OPTIONS = ['prowess', 'recent', 'searches'];

export function PlayerLeaderboardPanel({
  items,
  sort,
  loading,
  error,
  characterOptions,
  locale,
  t,
  onSortChange,
  onPlayerSelect,
}) {
  return (
    <section className="leaderboard-panel" aria-label={t('leaderboard.ariaLabel')}>
      <div className="section-header">
        <div>
          <p className="eyebrow">{t('leaderboard.eyebrow')}</p>
          <h2>{t('leaderboard.title')}</h2>
        </div>
        <div className="leaderboard-tabs" role="tablist" aria-label={t('leaderboard.sortLabel')}>
          {SORT_OPTIONS.map((option) => (
            <button
              key={option}
              type="button"
              className={sort === option ? 'active' : ''}
              aria-selected={sort === option}
              onClick={() => onSortChange(option)}
            >
              {t(`leaderboard.sort.${option}`)}
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div className="leaderboard-state">
          <Loader2 aria-hidden="true" className="spin" />
          <span>{t('leaderboard.loading')}</span>
        </div>
      )}

      {!loading && error && (
        <div className="leaderboard-state error">
          <span>{error}</span>
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="leaderboard-state">
          <Trophy aria-hidden="true" />
          <span>{t('leaderboard.empty')}</span>
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <div className="leaderboard-list">
          {items.map((item) => {
            const character = findCharacterOption(characterOptions, item.mainCharacter);
            const characterName = character?.localizedNames?.[locale] || character?.displayName || item.mainCharacter;

            return (
              <button
                key={item.tekkenId}
                type="button"
                className="leaderboard-row"
                onClick={() => onPlayerSelect(item.tekkenId)}
              >
                <span className="leaderboard-rank">#{item.rank}</span>
                <span className="leaderboard-player">
                  <CharacterPortrait character={character} label={item.mainCharacter} size="sm" />
                  <span>
                    <strong>{item.name || item.tekkenId}</strong>
                    <em>{item.tekkenId}</em>
                  </span>
                </span>
                <span className="leaderboard-character">
                  <strong>{displayValue(characterName)}</strong>
                  <em>{displayValue(item.danRank)}</em>
                </span>
                <span className="leaderboard-metric">
                  <strong>{displayValue(item.tekkenProwess)}</strong>
                  <em>{t('leaderboard.prowess')}</em>
                </span>
                <span className="leaderboard-meta">
                  <strong>{[item.region, item.platform].filter(Boolean).join(' / ') || '-'}</strong>
                  <em>{formatDateByLocale(item.lastUpdatedAt, locale)}</em>
                </span>
                <span className="leaderboard-search-count">
                  <Search aria-hidden="true" />
                  <span>{displayValue(item.searchCount)}</span>
                </span>
              </button>
            );
          })}
        </div>
      )}
    </section>
  );
}
