export function characterAssetPath(character) {
  if (!character?.assetKey) {
    return null;
  }
  return `/assets/characters/${character.assetKey}.webp`;
}

export function characterFallbackLabel(value) {
  if (!value || value === '-') {
    return '?';
  }
  return String(value)
    .split(/\s|-/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

export function characterAccentStyle(value) {
  const seed = String(value || 'unknown');
  const hash = Array.from(seed).reduce((current, character) => {
    return (current * 31 + character.charCodeAt(0)) % 360;
  }, 19);

  return {
    '--portrait-hue': hash,
    '--portrait-hue-shift': (hash + 28) % 360,
  };
}

export function findCharacterOption(characterOptions, characterName) {
  if (!characterName) {
    return null;
  }
  const normalizedName = normalizeCharacterName(characterName);
  return characterOptions.find((character) => {
    const values = [
      character.name,
      character.displayName,
      ...(character.aliases || []),
      ...Object.values(character.localizedNames || {}),
    ];
    return values.some((value) => normalizeCharacterName(value) === normalizedName);
  }) || null;
}

function normalizeCharacterName(value) {
  return String(value || '')
    .trim()
    .toLowerCase()
    .replaceAll(' ', '')
    .replaceAll('-', '');
}
