# Product Requirements

## Product Direction

T8LAB is a Tekken 8 match history and analysis platform. The first target is Korean Tekken 8 players, while the data model and UI structure should remain compatible with global expansion.

The product should reference the search-first structure of OP.GG, the Tekken-specific data available from EWGF.GG, the rating and leaderboard concepts from Wavu Wank, and the player-focused navigation patterns of TKNOW.GG. The implementation should not copy a single reference site directly.

## MVP Scope

The MVP focuses on player lookup and player detail analysis.

Included:
- Home search by Tekken ID.
- Player profile summary from EWGF Pro API.
- Recent battle history from EWGF API.
- Basic win/loss summary.
- Character usage and win rate summary.
- PostgreSQL-backed cache and snapshots.

Excluded from MVP:
- Login.
- Favorites.
- Global leaderboard.
- Wavu integration.
- Patch notes.
- Live feed.
- Community features.

## Initial Decisions

- Codename: T8LAB.
- Primary audience: Korean Tekken 8 players.
- Search priority: Tekken ID first.
- Data source: EWGF Pro API first.
- Database: PostgreSQL.
- Design tone: data-focused analysis dashboard.
- First release boundary: player search, profile detail, match history, and basic analysis.

## Core Pages

- `/`: Search-first home page.
- `/player/:tekkenId`: Player detail page.
- `/about`: Data source and service notes.

## Core Backend APIs

- `GET /api/players/{tekkenId}`
- `GET /api/players/{tekkenId}/matches`
- `GET /api/players/{tekkenId}/summary`
- `GET /api/players/{tekkenId}/character-stats`

## Data Policy

External provider response shapes must not leak directly into frontend contracts. The backend should map EWGF and future Wavu responses into service-owned DTOs.

API keys must be provided through environment variables and must never be committed.
