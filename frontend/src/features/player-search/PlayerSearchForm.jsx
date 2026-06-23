import { Loader2, Search } from 'lucide-react';

export const SAMPLE_TEKKEN_ID = '27tB-4yhF-mfNE';

export function PlayerSearchForm({ tekkenId, loading, onChange, onSubmit }) {
  return (
    <form className="search-card" onSubmit={onSubmit}>
      <label htmlFor="tekken-id">Tekken ID</label>
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
          <span>{loading ? '검색 중' : '검색'}</span>
        </button>
      </div>
      <button className="sample-button" type="button" onClick={() => onChange(SAMPLE_TEKKEN_ID)}>
        샘플 ID 입력
      </button>
    </form>
  );
}
