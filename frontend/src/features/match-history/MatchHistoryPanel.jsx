import { Activity, AlertCircle, Loader2, Trophy } from 'lucide-react';
import { CharacterPortrait } from '../../shared/components/CharacterPortrait';
import { findCharacterOption } from '../../shared/utils/characters';
import { displayValue, formatDateByLocale } from '../../shared/utils/formatters';
import { localizedBattleTypeLabel, localizedRankLabel } from '../../shared/utils/gameMetadata';

export function MatchHistoryPanel({ matches, characterOptions = [], locale = 'ko', t, loading, refreshing, error, total, hasMore, onLoadMore }) {
  return (
    <section className="matches-panel" aria-label={t('matches.ariaLabel')}>
      <div className="section-header">
        <div>
          <p className="eyebrow">{t('matches.eyebrow')}</p>
          <h2>{t('matches.title')}</h2>
        </div>
        <div className="section-actions">
          <span>{refreshing ? t('matches.refreshing') : loading && !matches.length ? t('matches.loading') : `${matches.length}/${total || matches.length} ${t('stats.games')}`}</span>
        </div>
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
            <strong>{t('matches.loadingTitle')}</strong>
            <p>{t('matches.loadingDescription')}</p>
          </div>
        </div>
      )}

      {!loading && !error && !matches.length && (
        <div className="empty-panel inline">
          <Activity aria-hidden="true" />
          <div>
            <strong>{t('matches.emptyTitle')}</strong>
            <p>{t('matches.emptyDescription')}</p>
          </div>
        </div>
      )}

      {!!matches.length && (
        <div className="match-list">
          {matches.map((match) => (
            <MatchRow
              key={match.externalMatchKey || `${match.battleAt}-${match.opponent?.tekkenId}`}
              match={match}
              characterOptions={characterOptions}
              locale={locale}
              t={t}
            />
          ))}
        </div>
      )}

      {hasMore && (
        <button className="load-more-button" type="button" onClick={onLoadMore} disabled={loading}>
          {loading ? <Loader2 aria-hidden="true" className="spin" /> : null}
          <span>{loading ? t('matches.loading') : t('matches.loadMore')}</span>
        </button>
      )}
    </section>
  );
}

function MatchRow({ match, characterOptions, locale, t }) {
  const isWin = match.result === 'WIN';
  const my = match.my || {};
  const opponent = match.opponent || {};
  const myCharacter = findCharacterOption(characterOptions, my.character);
  const opponentCharacter = findCharacterOption(characterOptions, opponent.character);

  return (
    <article className={`match-row ${isWin ? 'win' : 'loss'}`}>
      <div className="result-mark">
        <strong>{isWin ? t('matches.win') : match.result === 'LOSS' ? t('matches.loss') : '-'}</strong>
        <span>{localizedBattleTypeLabel(match.battleType, locale)}</span>
      </div>

      <div className="match-main">
        <div className="fighter self">
          <span className="fighter-label">{t('matches.me')}</span>
          <div className="fighter-character">
            <div>
              <strong>{displayValue(my.character)}</strong>
              <p>{displayValue(my.name)}</p>
            </div>
            <CharacterPortrait character={myCharacter} label={my.character} />
          </div>
          <em>{displayValue(localizedRankLabel(my.rank, locale))}</em>
        </div>

        <div className="versus">
          <Trophy aria-hidden="true" />
          <strong>{displayValue(match.roundScore)}</strong>
        </div>

        <div className="fighter">
          <span className="fighter-label">{t('matches.opponent')}</span>
          <div className="fighter-character">
            <CharacterPortrait character={opponentCharacter} label={opponent.character} />
            <div>
              <strong>{displayValue(opponent.character)}</strong>
              <p>{displayValue(opponent.name)}</p>
            </div>
          </div>
          <em>{displayValue(localizedRankLabel(opponent.rank, locale))}</em>
        </div>
      </div>

      <div className="match-meta">
        <span>{formatDateByLocale(match.battleAt, locale)}</span>
      </div>
    </article>
  );
}
