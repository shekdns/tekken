import { RotateCcw, Search, SlidersHorizontal } from 'lucide-react';

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

export function PlayerFiltersPanel({ filters, loading, onChange, onSubmit, onReset }) {
  const updateFilter = (key, value) => {
    onChange({ ...filters, [key]: value });
  };

  return (
    <section className="filters-panel" aria-label="전적 필터">
      <div className="filters-heading">
        <div>
          <p className="eyebrow">Filters</p>
          <h2>전적 기준</h2>
        </div>
        <SlidersHorizontal aria-hidden="true" />
      </div>

      <div className="filter-grid">
        <label>
          <span>전투 타입</span>
          <select value={filters.battleType} onChange={(event) => updateFilter('battleType', event.target.value)}>
            <option value="ALL">전체</option>
            <option value="RANKED_BATTLE">랭크 매치</option>
            <option value="QUICK_BATTLE">퀵 매치</option>
            <option value="PLAYER_BATTLE">플레이어 매치</option>
          </select>
        </label>

        <label>
          <span>내 캐릭터</span>
          <input
            value={filters.character}
            onChange={(event) => updateFilter('character', event.target.value)}
            placeholder="예: Dragunov"
          />
        </label>

        <label>
          <span>상대 캐릭터</span>
          <input
            value={filters.opponentCharacter}
            onChange={(event) => updateFilter('opponentCharacter', event.target.value)}
            placeholder="예: Bryan"
          />
        </label>

        <label>
          <span>기간</span>
          <select value={filters.days} onChange={(event) => updateFilter('days', event.target.value)}>
            <option value="ALL">전체</option>
            <option value="7">최근 7일</option>
            <option value="30">최근 30일</option>
            <option value="90">최근 90일</option>
          </select>
        </label>
      </div>

      <div className="filter-actions">
        <button className="secondary-action" type="button" onClick={onReset} disabled={loading}>
          <RotateCcw aria-hidden="true" />
          <span>초기화</span>
        </button>
        <button className="primary-action" type="button" onClick={onSubmit} disabled={loading}>
          <Search aria-hidden="true" />
          <span>{loading ? '적용 중' : '필터 적용'}</span>
        </button>
      </div>
    </section>
  );
}
