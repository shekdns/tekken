export function playerPath(tekkenId) {
  return `/players/${encodeURIComponent(tekkenId)}`;
}

export function tekkenIdFromPath(pathname = window.location.pathname) {
  const match = pathname.match(/^\/players\/([^/]+)$/);
  return match ? decodeURIComponent(match[1]) : '';
}

export function pushPlayerPath(tekkenId) {
  window.history.pushState({}, '', playerPath(tekkenId));
}

export function pushHomePath() {
  window.history.pushState({}, '', '/');
}
