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

export async function fetchPlayerMatches(tekkenId) {
  const response = await fetch(`/api/players/${encodeURIComponent(tekkenId)}/matches`);
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(body?.message || `최근 경기 정보를 가져오지 못했습니다. HTTP ${response.status}`);
  }

  const matches = Array.isArray(body?.matches) ? body.matches : [];
  return matches.map((match) => normalizeMatch(match, tekkenId));
}
