import { Flame, Loader2, Search, TimerReset } from 'lucide-react';

export const SAMPLE_TEKKEN_ID = '27tB-4yhF-mfNE';

function SuggestionGroup({ icon: Icon, title, items, emptyText, onSelect }) {
  return (
    <div className="suggestion-group">
      <div className="suggestion-heading">
        <Icon aria-hidden="true" />
        <span>{title}</span>
      </div>
      <div className="suggestion-list">
        {items.length > 0 ? (
          items.map((item) => (
            <button
              key={`${title}-${item.tekkenId}`}
              type="button"
              className="suggestion-chip"
              onClick={() => onSelect(item.tekkenId)}
            >
              <span>{item.displayTekkenId || item.query || item.tekkenId}</span>
              {item.searchCount > 1 && <em>{item.searchCount}</em>}
            </button>
          ))
        ) : (
          <span className="suggestion-empty">{emptyText}</span>
        )}
      </div>
    </div>
  );
}

export function PlayerSearchForm({
  tekkenId,
  loading,
  suggestionsLoading,
  recentSearches,
  popularSearches,
  t,
  onChange,
  onSubmit,
  onSuggestionSelect,
}) {
  return (
    <form className="search-card" onSubmit={onSubmit}>
      <label htmlFor="tekken-id">{t('search.label')}</label>
      <div className="search-input-row">
        <Search aria-hidden="true" />
        <input
          id="tekken-id"
          type="text"
          value={tekkenId}
          placeholder={SAMPLE_TEKKEN_ID}
          autoComplete="off"
          onChange={(event) => onChange(event.target.value)}
        />
        <button type="submit" disabled={loading}>
          {loading ? <Loader2 aria-hidden="true" className="spin" /> : <Search aria-hidden="true" />}
          <span>{loading ? t('search.searching') : t('search.submit')}</span>
        </button>
      </div>
      <button className="sample-button" type="button" onClick={() => onChange(SAMPLE_TEKKEN_ID)}>
        {t('search.sample')}
      </button>

      <div className="search-suggestions" aria-label={t('search.suggestionsAriaLabel')}>
        {suggestionsLoading ? (
          <div className="suggestion-loading">
            <Loader2 aria-hidden="true" className="spin" />
            <span>{t('search.suggestionsLoading')}</span>
          </div>
        ) : (
          <>
            <SuggestionGroup
              icon={TimerReset}
              title={t('search.recentSearches')}
              items={recentSearches}
              emptyText={t('search.noRecentSearches')}
              onSelect={onSuggestionSelect}
            />
            <SuggestionGroup
              icon={Flame}
              title={t('search.popularSearches')}
              items={popularSearches}
              emptyText={t('search.noPopularSearches')}
              onSelect={onSuggestionSelect}
            />
          </>
        )}
      </div>
    </form>
  );
}
