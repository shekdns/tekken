# T8LAB

Tekken 8 match history and analysis platform.

## 현재 진행 상황

이 섹션은 기능 단계가 끝날 때마다 업데이트합니다.

### 완료

1. 프로젝트 기본 구조를 `backend`와 `frontend`로 분리했습니다.
2. Spring Boot 백엔드와 React/Vite 프론트엔드 로컬 실행 흐름을 구성했습니다.
3. Docker/Colima 기반 PostgreSQL 로컬 DB 구성을 추가했습니다.
4. Flyway 초기 마이그레이션으로 핵심 테이블을 추가했습니다.
   - `players`
   - `matches`
   - `api_cache`
   - `player_search_history`
5. `EWGF_API_KEY` 환경변수 기반 EWGF API 호출을 확인했습니다.
6. 플레이어 검색 API를 추가했습니다.
   - `GET /api/players/{tekkenId}`
   - `GET /api/players/{tekkenId}/matches`
7. EWGF profile 응답을 T8LAB 표준 `summary` 응답으로 매핑했습니다.
8. 프론트엔드 검색 화면을 추가하고 플레이어 요약 정보를 표시했습니다.
9. 샘플 플레이어 기준 랭크, 주 캐릭터, 지역, 플랫폼, Prowess 표시를 확인했습니다.
10. EWGF battles 응답을 T8LAB 표준 match 응답으로 매핑했습니다.
11. 프론트엔드에 최근 경기 목록을 추가했습니다.
12. 최근 경기에서 승/패, 내 캐릭터, 상대 캐릭터, 상대 이름, 상대 랭크, 라운드 스코어, 경기 시간을 표시합니다.
13. 프론트엔드 코드를 기능별 디렉터리로 분리했습니다.
14. 최근 100게임 기준 기본 통계 패널을 추가했습니다.
15. 캐릭터별 사용 횟수와 승률을 프론트엔드에서 계산해 표시합니다.

### 현재 단계

- 플레이어 검색, 프로필 요약, 최근 경기 목록, 최근 경기 기반 통계 표시까지 완료된 상태입니다.
- 다음 단계는 라우팅 구조와 상세 페이지 URL을 추가하는 것입니다.

### 다음 작업

1. `/players/{tekkenId}` 상세 페이지 라우팅을 추가합니다.
2. 검색 후 상세 페이지 URL로 이동하는 흐름을 만듭니다.
3. frontend 통계 계산 로직을 추후 backend stats API로 이전할 기준을 정합니다.
4. TKNOW.GG/EWGF.GG를 참고해 상세 화면 디자인을 고도화합니다.

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
