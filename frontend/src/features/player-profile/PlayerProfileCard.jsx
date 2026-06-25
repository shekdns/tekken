import { Clock3, Database, Loader2, RefreshCw, UserRound } from 'lucide-react';
import { CharacterPortrait } from '../../shared/components/CharacterPortrait';
import { findCharacterOption } from '../../shared/utils/characters';
import { displayValue, formatDateByLocale } from '../../shared/utils/formatters';
import { localizedRankLabel } from '../../shared/utils/gameMetadata';
import { profileSummary } from '../../shared/utils/playerProfile';

export function PlayerProfileCard({ profile, characterOptions = [], locale = 'ko', t, refreshing = false, refreshDisabled = false, onRefresh }) {
  const summary = profileSummary(profile);
  const player = profile?.profile;
  const mainCharacter = findCharacterOption(characterOptions, summary?.character);

  if (!profile || !summary) {
    return null;
  }

  return (
    <section className="result-panel" aria-label={t('profile.ariaLabel')}>
      <div className="result-header">
        <div className="avatar">
          <UserRound aria-hidden="true" />
        </div>
        <div>
          <p className="eyebrow">{t('profile.eyebrow')}</p>
          <h2>{summary.name}</h2>
          <p>{profile.tekkenId}</p>
        </div>
        <div className="profile-actions">
          <div className={`source-badge ${profile.source === 'cache' ? 'cache' : 'live'}`}>
            <Database aria-hidden="true" />
            <span>{profile.source === 'cache' ? 'Cache' : 'EWGF'}</span>
          </div>

          {onRefresh && (
            <button
              className="refresh-record-button"
              type="button"
              onClick={onRefresh}
              disabled={refreshDisabled}
              aria-label={t('profile.refresh')}
            >
              {refreshing ? <Loader2 aria-hidden="true" className="spin" /> : <RefreshCw aria-hidden="true" />}
              <span>{refreshing ? t('profile.refreshing') : t('profile.refresh')}</span>
            </button>
          )}
        </div>
      </div>

      <div className="profile-spotlight">
        <CharacterPortrait character={mainCharacter} label={summary.character} size="lg" />
        <div>
          <span>{t('profile.character')}</span>
          <strong>{displayValue(summary.character)}</strong>
          <p>{localizedRankLabel(summary.rank, locale)}</p>
        </div>
      </div>

      <div className="stat-grid">
        <StatItem label={t('profile.rank')} value={localizedRankLabel(summary.rank, locale)} />
        <StatItem label={t('profile.character')} value={summary.character} />
        <StatItem label={t('profile.region')} value={summary.region} />
        <StatItem label={t('profile.prowess')} value={summary.prowess} />
        <StatItem label={t('profile.platform')} value={summary.platform} />
        <StatItem label={t('profile.fetchedAt')} value={formatDateByLocale(profile.fetchedAt, locale)} icon={<Clock3 aria-hidden="true" />} />
      </div>

      <details className="raw-data">
        <summary>{t('profile.rawData')}</summary>
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
