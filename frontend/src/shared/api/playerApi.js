import { normalizeMatch } from '../utils/matches';

export async function fetchHealth() {
  const response = await fetch('/api/health');
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}

export async function fetchPlayerProfile(tekkenId) {
  const response = await fetch(`/api/players/${encodeURIComponent(tekkenId)}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(body?.message || `플레이어 정보를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return body;
}

export async function fetchPlayerMatches(tekkenId, options = {}) {
  const params = new URLSearchParams();
  if (options.offset !== undefined) {
    params.set('offset', String(options.offset));
  }
  if (options.limit) {
    params.set('limit', String(options.limit));
  }
  if (options.battleType) {
    params.set('battleType', options.battleType);
  }
  if (options.character) {
    params.set('character', options.character);
  }
  if (options.opponentCharacter) {
    params.set('opponentCharacter', options.opponentCharacter);
  }
  if (options.days) {
    params.set('days', String(options.days));
  }
  if (options.refresh) {
    params.set('refresh', 'true');
  }

  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`/api/players/${encodeURIComponent(tekkenId)}/matches${query}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(body?.message || `최근 경기 정보를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  const matches = Array.isArray(body?.matches) ? body.matches : [];
  return {
    ...body,
    matches: matches.map((match) => normalizeMatch(match, tekkenId)),
  };
}

export async function fetchPlayerStats(tekkenId, options = {}) {
  const params = new URLSearchParams();
  if (options.limit) {
    params.set('limit', String(options.limit));
  }
  if (options.battleType) {
    params.set('battleType', options.battleType);
  }
  if (options.character) {
    params.set('character', options.character);
  }
  if (options.opponentCharacter) {
    params.set('opponentCharacter', options.opponentCharacter);
  }
  if (options.days) {
    params.set('days', String(options.days));
  }

  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`/api/players/${encodeURIComponent(tekkenId)}/stats${query}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(body?.message || `최근 통계를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return body;
}
