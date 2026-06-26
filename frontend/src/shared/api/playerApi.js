import { normalizeMatch } from '../utils/matches';

function apiError(body, fallbackMessage) {
  const error = new Error(body?.message || fallbackMessage);
  error.code = body?.code || null;
  error.upstreamStatus = body?.upstreamStatus || null;
  error.path = body?.path || null;
  return error;
}

export async function fetchHealth() {
  const response = await fetch('/api/health');
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}

export async function fetchPlayerProfile(tekkenId, options = {}) {
  const params = new URLSearchParams();
  if (options.refresh) {
    params.set('refresh', 'true');
  }

  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`/api/players/${encodeURIComponent(tekkenId)}${query}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `플레이어 정보를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return body;
}

export async function fetchCharacterOptions() {
  const response = await fetch('/api/characters/options');
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `캐릭터 옵션을 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return Array.isArray(body?.characters) ? body.characters : [];
}

export async function fetchRecentSearches(limit = 10) {
  const params = new URLSearchParams();
  params.set('limit', String(limit));

  const response = await fetch(`/api/search/recent?${params.toString()}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `최근 검색어를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return Array.isArray(body?.items) ? body.items : [];
}

export async function fetchPopularSearches({ days = 7, limit = 10 } = {}) {
  const params = new URLSearchParams();
  params.set('days', String(days));
  params.set('limit', String(limit));

  const response = await fetch(`/api/search/popular?${params.toString()}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `인기 검색어를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return Array.isArray(body?.items) ? body.items : [];
}

export async function fetchPlayerAutocomplete(query, limit = 8) {
  const params = new URLSearchParams();
  params.set('q', query);
  params.set('limit', String(limit));

  const response = await fetch(`/api/search/players?${params.toString()}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `플레이어 검색 후보를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return Array.isArray(body?.items) ? body.items : [];
}

export async function fetchPlayerLeaderboard(options = {}) {
  const params = new URLSearchParams();
  params.set('sort', options.sort || 'prowess');
  params.set('limit', String(options.limit || 10));
  if (options.character) {
    params.set('character', options.character);
  }
  if (options.region) {
    params.set('region', options.region);
  }
  if (options.platform) {
    params.set('platform', options.platform);
  }

  const response = await fetch(`/api/leaderboards/players?${params.toString()}`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw apiError(body, `리더보드를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return {
    ...body,
    items: Array.isArray(body?.items) ? body.items : [],
  };
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
    throw apiError(body, `최근 경기 정보를 가져오지 못했습니다. HTTP ${response.status}`);
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
    throw apiError(body, `최근 통계를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  return body;
}
