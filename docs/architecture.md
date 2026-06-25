# Architecture

## Overview

The project is a Spring Boot backend and React frontend monorepo.

The backend owns all external API access and all database persistence. The frontend talks only to the backend API.

```text
Frontend
  -> Backend API
    -> Domain Services
      -> Data Providers
        -> EWGF API
        -> Wavu API later
      -> PostgreSQL
```

## Backend Package Direction

```text
com.project.tekken
  common
    config
    error
    response

  datasource
    ewgf
    wavu

  player
  match
  stats
  leaderboard
  cache
```

Current code can be moved incrementally. The initial `external` package can remain until the first player API is introduced.

## Database Direction

PostgreSQL is the development and production database target.

Initial responsibilities:
- Cache EWGF responses.
- Store player profile snapshots.
- Store battle snapshots.
- Track search history.

Long-term responsibilities:
- Build character statistics.
- Build leaderboards.
- Combine EWGF and Wavu data.
- Support user favorites and accounts.

## Discovery Direction

Search suggestions and leaderboards are designed in `docs/leaderboard-search-history.md`.

Initial discovery features should use existing PostgreSQL data first:

- `player_search_history` for recent and popular searches.
- `players` snapshots for internal T8LAB leaderboards.

External leaderboard sources such as Wavu should be integrated later through a datasource boundary, without leaking provider-specific response shapes to the frontend.

## Local Development

PostgreSQL should be started through Docker Compose so macOS and Windows use the same database setup.

```bash
docker compose up -d
```

Backend environment variables:

```text
EWGF_API_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
```

## Cross-Platform Notes

macOS/Linux:

```bash
cd backend
sh gradlew bootRun
```

Windows PowerShell:

```powershell
cd backend
.\gradlew.bat bootRun
```

Environment variables should be set outside the repository or through local developer tooling. Secret values should not be committed.

## Frontend Asset Direction

Character images are served from the Vite public asset path:

```text
frontend/public/assets/characters/{assetKey}.webp
```

The `assetKey` comes from the backend character option API. If a file is missing, the frontend must keep rendering a safe fallback.

Production image files require source tracking through the asset policy in `docs/asset-policy.md`.
