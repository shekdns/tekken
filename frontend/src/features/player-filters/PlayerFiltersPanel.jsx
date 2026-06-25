import { RotateCcw, Search, SlidersHorizontal } from 'lucide-react';
import { BATTLE_TYPE_OPTIONS, localizedBattleTypeLabel } from '../../shared/utils/gameMetadata';

export const DEFAULT_PLAYER_FILTERS = {
  battleType: 'ALL',
  character: '',
  opponentCharacter: '',
  days: 'ALL',
};

export function toApiFilters(filters) {
  return {
    battleType: filters.battleType === 'ALL' ? undefined : filters.battleType,
    character: filters.character.trim() || undefined,
    opponentCharacter: filters.opponentCharacter.trim() || undefined,
    days: filters.days === 'ALL' ? undefined : Number(filters.days),
  };
}

export function PlayerFiltersPanel({ filters, characterOptions = [], locale = 'ko', t, loading, onChange, onSubmit, onReset }) {
  const updateFilter = (key, value) => {
    onChange({ ...filters, [key]: value });
  };
  const characterLabel = (character) => {
    const localizedName = character.localizedNames?.[locale] || character.localizedNames?.en;
    if (localizedName && localizedName !== character.name) {
      return `${localizedName} (${character.displayName || character.name})`;
    }
    return character.displayName || character.name;
  };

  return (
    <section className="filters-panel" aria-label={t('filters.ariaLabel')}>
      <div className="filters-heading">
        <div>
          <p className="eyebrow">{t('filters.eyebrow')}</p>
          <h2>{t('filters.title')}</h2>
        </div>
        <SlidersHorizontal aria-hidden="true" />
      </div>

      <div className="filter-grid">
        <label>
          <span>{t('filters.battleType')}</span>
          <select value={filters.battleType} onChange={(event) => updateFilter('battleType', event.target.value)}>
            {BATTLE_TYPE_OPTIONS.map((battleType) => (
              <option key={battleType} value={battleType}>
                {localizedBattleTypeLabel(battleType, locale)}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>{t('filters.myCharacter')}</span>
          <select
            value={filters.character}
            onChange={(event) => updateFilter('character', event.target.value)}
          >
            <option value="">{t('filters.all')}</option>
            {characterOptions.map((character) => (
              <option key={character.id} value={character.name}>
                {characterLabel(character)}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>{t('filters.opponentCharacter')}</span>
          <select
            value={filters.opponentCharacter}
            onChange={(event) => updateFilter('opponentCharacter', event.target.value)}
          >
            <option value="">{t('filters.all')}</option>
            {characterOptions.map((character) => (
              <option key={character.id} value={character.name}>
                {characterLabel(character)}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>{t('filters.period')}</span>
          <select value={filters.days} onChange={(event) => updateFilter('days', event.target.value)}>
            <option value="ALL">{t('filters.all')}</option>
            <option value="7">{t('filters.last7Days')}</option>
            <option value="30">{t('filters.last30Days')}</option>
            <option value="90">{t('filters.last90Days')}</option>
          </select>
        </label>
      </div>

      <div className="filter-actions">
        <button className="secondary-action" type="button" onClick={onReset} disabled={loading}>
          <RotateCcw aria-hidden="true" />
          <span>{t('filters.reset')}</span>
        </button>
        <button className="primary-action" type="button" onClick={onSubmit} disabled={loading}>
          <Search aria-hidden="true" />
          <span>{loading ? t('filters.applying') : t('filters.apply')}</span>
        </button>
      </div>
    </section>
  );
}
