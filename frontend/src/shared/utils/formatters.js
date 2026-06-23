export function displayValue(value) {
  if (value === undefined || value === null || value === '') {
    return '-';
  }
  if (typeof value === 'number') {
    return new Intl.NumberFormat('ko-KR').format(value);
  }
  return value;
}

export function formatDate(value) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

export function battleTypeLabel(value) {
  if (value === 'RANKED_BATTLE') {
    return 'Ranked';
  }
  if (value === 'QUICK_BATTLE') {
    return 'Quick';
  }
  if (value === 'PLAYER_BATTLE') {
    return 'Player';
  }
  return value || '-';
}
