# T8LAB

Tekken 8 match history and analysis platform.

## Product Direction

T8LAB starts with EWGF Pro API-backed player lookup, recent match history, and basic player analysis. The project is structured to add PostgreSQL-backed caching, leaderboards, Wavu data, and richer statistics over time.

See:

- `docs/product-requirements.md`
- `docs/architecture.md`

## Requirements

- Java 17
- Node.js 20 or newer
- Docker Desktop

## Local Database

```bash
docker compose up -d
```

PostgreSQL defaults:

```text
database: tekken
user: tekken
password: tekken
port: 5432
```

## Backend

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

API health check: `GET http://localhost:8080/api/health`

Required environment variable:

```text
EWGF_API_KEY
```

Optional database environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/tekken
DB_USERNAME=tekken
DB_PASSWORD=tekken
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server: `http://localhost:5173`
