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
16. `/players/{tekkenId}` 상세 URL 라우팅을 추가했습니다.
17. 검색 성공 시 플레이어 상세 URL로 이동하고, 해당 URL 직접 접근 시 데이터를 자동 조회합니다.
18. `GET /api/players/{tekkenId}/stats` backend 통계 API를 추가했습니다.
19. 프론트엔드는 backend stats 응답을 우선 사용하고, 실패 시 matches 기반 계산으로 fallback합니다.
20. stats API에 `limit`, `battleType` 필터를 추가했습니다.
21. stats API에 battle type별 통계와 상대 캐릭터별 전적을 추가했습니다.
22. 프론트 통계 패널에 battle type별 승률과 상대 캐릭터별 전적을 표시했습니다.
23. stats API에 `character`, `opponentCharacter`, `days` 필터를 추가했습니다.
24. matches API에 `offset`, `limit`, `hasMore`, `nextOffset` 기반 더보기 계약을 추가했습니다.
25. 프론트 최근 경기 목록에 `더 보기` 버튼을 연결했습니다.
26. matches API에 `battleType`, `character`, `opponentCharacter`, `days` 필터 계약을 추가했습니다.
27. 프론트 API 클라이언트에서 최근 경기 필터 파라미터를 보낼 수 있게 준비했습니다.
28. 프로필 아래에 공통 전적 필터 UI를 추가했습니다.
29. 필터 적용 시 stats API와 matches API가 같은 기준으로 다시 조회되도록 연결했습니다.
30. `PlayerStatsCalculator` 테스트를 추가해 필터/승률/상세 통계 계산을 검증했습니다.
31. `PlayerMatchMapper` 테스트를 추가해 EWGF battles 응답의 snake_case/camelCase 매핑, my/opponent 분리, 승패 계산을 검증했습니다.
32. `PlayerProfileMapper` 테스트를 추가해 profile 응답의 nested field, `main_character` 기반 캐릭터/랭크 매핑을 검증했습니다.
33. `PlayerService` 테스트를 추가해 profile/matches 캐시 hit/miss, match 필터/페이징, stats 계산 연결, EWGF 오류 처리를 검증했습니다.
34. EWGF 외부 API 계층을 `external` 패키지에서 `datasource.ewgf` 패키지로 이동했습니다.
35. EWGF battles 호출 실패 시 저장된 DB match 데이터로 fallback하는 흐름을 추가했습니다.
36. `MatchEntity`를 `PlayerMatchSummary`로 복원하는 매핑 테스트를 추가했습니다.
37. match cache/API/DB fallback/저장 책임을 `PlayerMatchSyncService`로 분리했습니다.
38. `player` 하위 코드를 `controller`, `service`, `dto`, `mapper`, `stats`, `exception` 패키지로 분리했습니다.
39. DB match 조회/fallback 변환 책임을 `PlayerMatchQueryService`로 분리했습니다.
40. fresh cache가 없더라도 DB match 데이터가 최근 10분 이내면 EWGF 호출 없이 DB를 우선 사용하도록 정책을 추가했습니다.
41. matches API에 `refresh=true` 강제 갱신 옵션을 추가했습니다.
42. 프론트 최근 경기 패널에 갱신 버튼을 추가했습니다.
43. DB 저장 매치를 기준으로 matches API의 필터와 더보기 응답을 생성하는 조회 구조를 추가했습니다.

### 현재 단계

- 플레이어 검색, 프로필 요약, 공통 전적 필터, 최근 경기 목록/더보기/강제 갱신, DB 기준 매치 필터/더보기 조회, backend 통계 API 필터/상세 지표, mapper/service 테스트, match sync/query 분리, 역할별 패키지 분리, DB match fresh 우선 사용/fallback, 프론트 통계 표시, 상세 URL 라우팅까지 완료된 상태입니다.
- 다음 단계는 저장 데이터 기반 통계 확장 범위를 정리하는 것입니다.

### 다음 작업

1. TKNOW.GG/EWGF.GG를 참고해 상세 화면 디자인을 고도화합니다.
2. DB 저장 데이터 기반 통계로 확장할 범위를 설계합니다.
3. 캐릭터 필터를 직접 입력에서 자동완성 또는 콤보박스 방식으로 개선합니다.
4. 리더보드와 검색 이력 기반 추천 검색을 설계합니다.

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
