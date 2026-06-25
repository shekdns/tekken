const DEFAULT_LOCALE = 'ko';

const BATTLE_TYPE_LABELS = {
  ALL: {
    ko: '전체',
    en: 'All',
    ja: 'すべて',
  },
  RANKED_BATTLE: {
    ko: '랭크 매치',
    en: 'Ranked Match',
    ja: 'ランクマッチ',
  },
  QUICK_BATTLE: {
    ko: '퀵 매치',
    en: 'Quick Match',
    ja: 'クイックマッチ',
  },
  PLAYER_BATTLE: {
    ko: '플레이어 매치',
    en: 'Player Match',
    ja: 'プレイヤーマッチ',
  },
};

const RANK_LABELS = {
  Beginner: {
    ko: '초보자',
    en: 'Beginner',
    ja: '初心者',
  },
  '1st Dan': {
    ko: '1단',
    en: '1st Dan',
    ja: '1段',
  },
  '2nd Dan': {
    ko: '2단',
    en: '2nd Dan',
    ja: '2段',
  },
  Fighter: {
    ko: '파이터',
    en: 'Fighter',
    ja: 'ファイター',
  },
  Strategist: {
    ko: '전략가',
    en: 'Strategist',
    ja: 'ストラテジスト',
  },
  Combatant: {
    ko: '컴뱃턴트',
    en: 'Combatant',
    ja: 'コンバタント',
  },
  Brawler: {
    ko: '브롤러',
    en: 'Brawler',
    ja: 'ブローラー',
  },
  Ranger: {
    ko: '레인저',
    en: 'Ranger',
    ja: 'レンジャー',
  },
  Cavalry: {
    ko: '캐벌리',
    en: 'Cavalry',
    ja: 'キャバルリー',
  },
  Warrior: {
    ko: '워리어',
    en: 'Warrior',
    ja: 'ウォリアー',
  },
  Assailant: {
    ko: '어세일런트',
    en: 'Assailant',
    ja: 'アサイラント',
  },
  Dominator: {
    ko: '도미네이터',
    en: 'Dominator',
    ja: 'ドミネーター',
  },
  Vanquisher: {
    ko: '뱅퀴셔',
    en: 'Vanquisher',
    ja: 'ヴァンキッシャー',
  },
  Destroyer: {
    ko: '디스트로이어',
    en: 'Destroyer',
    ja: 'デストロイヤー',
  },
  Eliminator: {
    ko: '엘리미네이터',
    en: 'Eliminator',
    ja: 'エリミネーター',
  },
  Garyu: {
    ko: 'Garyu',
    en: 'Garyu',
    ja: '臥龍',
  },
  Shinryu: {
    ko: 'Shinryu',
    en: 'Shinryu',
    ja: '真龍',
  },
  Tenryu: {
    ko: 'Tenryu',
    en: 'Tenryu',
    ja: '天龍',
  },
  'Mighty Ruler': {
    ko: 'Mighty Ruler',
    en: 'Mighty Ruler',
    ja: 'Mighty Ruler',
  },
  'Flame Ruler': {
    ko: 'Flame Ruler',
    en: 'Flame Ruler',
    ja: 'Flame Ruler',
  },
  'Battle Ruler': {
    ko: 'Battle Ruler',
    en: 'Battle Ruler',
    ja: 'Battle Ruler',
  },
  Fujin: {
    ko: 'Fujin',
    en: 'Fujin',
    ja: '風神',
  },
  Raijin: {
    ko: 'Raijin',
    en: 'Raijin',
    ja: '雷神',
  },
  Kishin: {
    ko: 'Kishin',
    en: 'Kishin',
    ja: '鬼神',
  },
  Bushin: {
    ko: 'Bushin',
    en: 'Bushin',
    ja: '武神',
  },
  'Tekken King': {
    ko: 'Tekken King',
    en: 'Tekken King',
    ja: 'Tekken King',
  },
  'Tekken Emperor': {
    ko: 'Tekken Emperor',
    en: 'Tekken Emperor',
    ja: 'Tekken Emperor',
  },
  'Tekken God': {
    ko: 'Tekken God',
    en: 'Tekken God',
    ja: 'Tekken God',
  },
  'Tekken God Supreme': {
    ko: 'Tekken God Supreme',
    en: 'Tekken God Supreme',
    ja: 'Tekken God Supreme',
  },
  'God of Destruction': {
    ko: 'God of Destruction',
    en: 'God of Destruction',
    ja: 'God of Destruction',
  },
};

export const BATTLE_TYPE_OPTIONS = Object.keys(BATTLE_TYPE_LABELS);

export function localizedBattleTypeLabel(value, locale = DEFAULT_LOCALE) {
  return localizedMetadataLabel(BATTLE_TYPE_LABELS, value, locale);
}

export function localizedRankLabel(value, locale = DEFAULT_LOCALE) {
  return localizedMetadataLabel(RANK_LABELS, value, locale);
}

function localizedMetadataLabel(dictionary, value, locale) {
  if (!value) {
    return '-';
  }

  const normalizedValue = String(value).trim();
  const labels = dictionary[normalizedValue];
  if (!labels) {
    return normalizedValue;
  }

  return labels[locale] || labels.en || labels[DEFAULT_LOCALE] || normalizedValue;
}
