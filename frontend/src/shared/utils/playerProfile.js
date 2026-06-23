import { displayValue } from './formatters';

function pickValue(source, keys, fallback = '-') {
  for (const key of keys) {
    const value = source?.[key];
    if (value !== undefined && value !== null && value !== '') {
      return value;
    }
  }
  return fallback;
}

function objectValue(source, keys) {
  for (const key of keys) {
    const value = source?.[key];
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return value;
    }
  }
  return null;
}

function mainCharacterName(profile) {
  const mainCharacter = objectValue(profile, [
    'main_character',
    'mainCharacter',
    'favorite_character',
    'favoriteCharacter',
    'most_played_character',
    'mostPlayedCharacter',
    'character',
  ]);

  if (!mainCharacter) {
    return pickValue(profile, [
      'main_character',
      'mainCharacter',
      'favoriteCharacter',
      'mostPlayedCharacter',
      'character',
      'character_name',
      'characterName',
    ]);
  }

  const namedValue = pickValue(mainCharacter, ['name', 'character', 'character_name', 'characterName'], null);
  if (namedValue) {
    return namedValue;
  }

  const entries = Object.entries(mainCharacter);
  return entries.length === 1 ? entries[0][0] : '-';
}

function mainCharacterRank(profile) {
  const mainCharacter = objectValue(profile, [
    'main_character',
    'mainCharacter',
    'favorite_character',
    'favoriteCharacter',
    'most_played_character',
    'mostPlayedCharacter',
    'character',
  ]);

  const directRank = pickValue(profile, ['rank', 'rankName', 'dan_rank', 'danRank', 'highestRank'], null);
  if (directRank) {
    return directRank;
  }

  if (!mainCharacter) {
    return '-';
  }

  const nestedRank = pickValue(mainCharacter, ['rank', 'rankName', 'dan_rank', 'danRank', 'highestRank'], null);
  if (nestedRank) {
    return nestedRank;
  }

  const entries = Object.entries(mainCharacter);
  return entries.length === 1 ? entries[0][1] : '-';
}

export function profileSummary(profile) {
  const player = profile?.profile;
  if (!profile) {
    return null;
  }

  if (profile.summary) {
    return {
      name: displayValue(profile.summary.name) === '-' ? 'Unknown Player' : profile.summary.name,
      rank: displayValue(profile.summary.rank),
      character: displayValue(profile.summary.mainCharacter),
      region: displayValue(profile.summary.region),
      prowess: displayValue(profile.summary.tekkenProwess),
      platform: displayValue(profile.summary.platform),
    };
  }

  return {
    name: pickValue(player, ['name', 'nickname', 'displayName', 'playerName', 'steamName'], 'Unknown Player'),
    rank: mainCharacterRank(player),
    character: mainCharacterName(player),
    region: pickValue(player, ['region', 'country', 'area']),
    prowess: displayValue(pickValue(player, ['tekken_prowess', 'tekkenProwess', 'prowess', 'power'])),
    platform: pickValue(player, ['platform', 'network', 'provider']),
  };
}
