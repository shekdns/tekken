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
