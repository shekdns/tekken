export function normalizeTekkenId(value) {
  return value ? String(value).replaceAll('-', '') : '';
}

export function normalizeMatch(match, targetTekkenId) {
  if (match?.my && match?.opponent) {
    return match;
  }

  const normalizedTarget = normalizeTekkenId(targetTekkenId);
  const p1 = participant(match, 1);
  const p2 = participant(match, 2);
  const my = p1.tekkenId === normalizedTarget ? p1 : p2;
  const opponent = my.side === 1 ? p2 : p1;
  const winnerSide = match?.winner;

  return {
    externalMatchKey: match?.externalMatchKey || match?.id || `${match?.battle_at || match?.battleAt}-${p1.tekkenId}-${p2.tekkenId}`,
    battleAt: match?.battleAt || match?.battle_at,
    battleType: match?.battleType || match?.battle_type,
    result: winnerSide ? (winnerSide === my.side ? 'WIN' : 'LOSS') : 'UNKNOWN',
    winnerSide,
    stageId: match?.stageId || match?.stage_id,
    gameVersion: match?.gameVersion || match?.game_version,
    roundScore: my.roundsWon !== undefined && opponent.roundsWon !== undefined ? `${my.roundsWon}-${opponent.roundsWon}` : null,
    my,
    opponent,
    raw: match,
  };
}

export function calculateMatchStats(matches) {
  const total = matches.length;
  const wins = matches.filter((match) => match.result === 'WIN').length;
  const losses = matches.filter((match) => match.result === 'LOSS').length;
  const winRate = total ? Math.round((wins / total) * 100) : 0;
  const recent10 = matches.slice(0, 10);
  const recent10Wins = recent10.filter((match) => match.result === 'WIN').length;
  const characterStats = characterUsage(matches);
  const mostPlayedCharacter = characterStats[0]?.character || '-';

  return {
    total,
    wins,
    losses,
    winRate,
    recent10Wins,
    recent10Losses: Math.max(recent10.length - recent10Wins, 0),
    recent10Record: `${recent10Wins}승 ${Math.max(recent10.length - recent10Wins, 0)}패`,
    mostPlayedCharacter,
    characterStats,
  };
}

function participant(match, side) {
  const snakePrefix = `p${side}_`;
  const camelPrefix = `p${side}`;

  return {
    side,
    tekkenId: normalizeTekkenId(match?.[`${snakePrefix}tekken_id`] || match?.[`${camelPrefix}TekkenId`]),
    name: match?.[`${snakePrefix}name`] || match?.[`${camelPrefix}Name`],
    character: match?.[`${snakePrefix}char`] || match?.[`${camelPrefix}Char`] || match?.[`${snakePrefix}character`] || match?.[`${camelPrefix}Character`],
    region: match?.[`${snakePrefix}region`] || match?.[`${camelPrefix}Region`],
    rank: match?.[`${snakePrefix}dan_rank`] || match?.[`${camelPrefix}DanRank`],
    tekkenPower: match?.[`${snakePrefix}tekken_power`] || match?.[`${camelPrefix}TekkenPower`],
    roundsWon: match?.[`${snakePrefix}rounds_won`] || match?.[`${camelPrefix}RoundsWon`],
  };
}

function characterUsage(matches) {
  const stats = new Map();
  for (const match of matches) {
    const character = match.my?.character || '-';
    const current = stats.get(character) || { character, games: 0, wins: 0, winRate: 0 };
    current.games += 1;
    if (match.result === 'WIN') {
      current.wins += 1;
    }
    current.winRate = Math.round((current.wins / current.games) * 100);
    stats.set(character, current);
  }

  return Array.from(stats.values())
    .sort((a, b) => b.games - a.games || b.winRate - a.winRate)
    .slice(0, 5);
}
