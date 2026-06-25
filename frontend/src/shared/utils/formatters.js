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

export function formatDateByLocale(value, locale = 'ko') {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(toIntlLocale(locale), {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

function toIntlLocale(locale) {
  if (locale === 'en') {
    return 'en-US';
  }
  if (locale === 'ja') {
    return 'ja-JP';
  }
  return 'ko-KR';
}
