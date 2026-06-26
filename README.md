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
44. stats API가 DB 저장 매치를 우선 사용해 승률, 캐릭터별 통계, 상대 캐릭터별 전적을 계산하도록 확장했습니다.
45. 캐릭터 옵션 API를 추가하고 프론트 필터를 직접 입력에서 선택형으로 변경했습니다.
46. 프로필과 전적을 함께 강제 갱신하는 전체 갱신 흐름을 추가했습니다.
47. 캐릭터 옵션 API에 한글명과 이미지 자산 키를 추가했습니다.
48. 캐릭터 메타데이터를 한국어(`ko`), 영어(`en`), 일본어(`ja`) `localizedNames` 구조로 확장했습니다.
49. 캐릭터 이미지 자산 경로 규칙과 프론트 fallback 초상화 표시를 추가했습니다.
50. 플레이어 상세 화면을 사이드바와 본문 중심의 전적 대시보드 레이아웃으로 1차 고도화했습니다.
51. 전체 갱신 액션을 프로필 카드 상단의 `전적 갱신` 버튼으로 이동해 사용 흐름을 정리했습니다.
52. 랭크와 전투 타입 표시명을 한국어(`ko`), 영어(`en`), 일본어(`ja`) locale 메타데이터 기반으로 표시하도록 프론트 구조를 추가했습니다.
53. 주요 웹 UI 문구를 한국어(`ko`), 영어(`en`), 일본어(`ja`) 리소스로 분리하고 상단 언어 선택 UI를 추가했습니다.
54. 캐릭터 이미지 자산 수급/라이선스 기준을 `docs/asset-policy.md`로 정리하고, 공식/제3자 이미지는 권리 확인 전까지 보류하는 정책을 추가했습니다.
55. 캐릭터 이름/assetKey 기반 색상과 패턴을 사용하는 T8LAB 전용 초상화 placeholder UI를 추가했습니다.
56. 프로필 주캐릭터 spotlight, 승률 강조 카드, 최근 경기 결과 배지를 추가해 상세 화면을 전적 사이트 톤으로 2차 고도화했습니다.
57. stats API 응답에 현재 연승/연패, 최장 연승/연패, 첫/최근 경기 시각, 활동일 수를 계산하는 확장 통계를 추가했습니다.
58. stats API의 `streakStats`, `activityStats`를 프론트 통계 패널 카드로 연결했습니다.
59. 백엔드 API 오류 응답에 `code`를 추가하고, 프론트에서 locale 기반 오류 문구로 표시하도록 정리했습니다.
60. 리더보드와 검색 이력 기반 추천 검색의 API/DB/UI 확장 방향을 `docs/leaderboard-search-history.md`로 정리했습니다.
61. 검색 이력 기반 추천 검색 API를 추가했습니다.
    - `GET /api/search/recent`
    - `GET /api/search/popular`
62. 추천 검색 API의 최근 검색 중복 제거, limit 보정, 인기 검색 응답 매핑 테스트를 추가했습니다.
63. 검색 홈에 최근 검색/인기 검색 추천 UI를 연결했습니다.
64. 추천 검색 문구를 한국어(`ko`), 영어(`en`), 일본어(`ja`) i18n 리소스에 추가했습니다.
65. T8LAB 내부 플레이어 리더보드 API를 추가했습니다.
    - `GET /api/leaderboards/players`
    - `sort=prowess|recent|searches`
    - `character`, `region`, `platform`, `limit` 필터
66. 리더보드 서비스 테스트를 추가해 prowess 정렬, 검색 횟수 정렬, 필터 적용을 검증했습니다.
67. 프론트 검색 화면에 내부 리더보드 UI를 연결했습니다.
    - Prowess
    - 최근 갱신
    - 검색 인기
68. Wavu 연동을 위한 `datasource.wavu` 패키지 경계를 추가했습니다.
69. Wavu 연동 원칙과 단계별 확장 방향을 `docs/wavu-datasource-plan.md`로 정리했습니다.
70. 닉네임/Tekken ID 자동완성 검색 API/UI 방향을 `docs/leaderboard-search-history.md`에 정리했습니다.
71. 닉네임/Tekken ID 자동완성 검색 backend API를 추가했습니다.
    - `GET /api/search/players?q={query}&limit=10`
72. 자동완성 검색 서비스 테스트를 추가해 짧은 검색어, players 우선순위, search history fallback, limit 보정을 검증했습니다.
73. Wavu `/player/search?q={query}` HTML 응답을 파싱해 자동완성 검색 후보에 병합하는 구조를 추가했습니다.
74. Wavu 검색 파싱 테스트와 자동완성 Wavu fallback 테스트를 추가했습니다.
75. 프론트 검색창에 닉네임/Tekken ID 자동완성 dropdown UI를 연결했습니다.
76. 자동완성 문구를 한국어(`ko`), 영어(`en`), 일본어(`ja`) i18n 리소스에 추가했습니다.
77. 닉네임 입력 후 Enter 검색 시 자동완성 후보의 Tekken ID로 상세 조회되도록 검색 흐름을 정리했습니다.
78. Wavu 자동완성 결과를 `api_cache`에 10분 TTL로 저장하도록 캐싱을 추가했습니다.
79. `WAVU_ENABLED=true` 상태에서 `/api/search/players?q=lowhigh&limit=5` 실제 조회와 cache 저장을 확인했습니다.

### 현재 단계

- 플레이어 검색, 프로필 요약, 공통 전적 필터, 캐릭터 선택 옵션/다국어 메타데이터 API, 랭크/전투 타입 locale 메타데이터, 주요 UI 문구 i18n 리소스와 언어 선택 UI, locale 기반 API 오류 표시, 캐릭터 이미지 자산 fallback 구조와 자산 정책, T8LAB 전용 초상화 placeholder, 최근 경기 목록/더보기/전체 갱신, DB 기준 매치 필터/더보기 조회, DB 기준 stats API 계산과 streak/activity 확장 통계, mapper/service 테스트, match sync/query 분리, 역할별 패키지 분리, DB match fresh 우선 사용/fallback, 프론트 통계 표시, 상세 URL 라우팅, 상세 화면 대시보드 레이아웃과 2차 디자인 고도화, 리더보드/추천 검색 설계, 추천 검색 API, 검색 홈 추천 검색 UI, 내부 플레이어 리더보드 API/UI, Wavu datasource 경계, Wavu 기반 자동완성 후보 병합, 자동완성 검색 backend API/UI까지 완료된 상태입니다.
- 다음 단계는 리더보드 필터 UI를 확장하거나, Wavu 검색 cache를 검색 snapshot 테이블로 분리할지 검토하는 것입니다.

### 다음 작업

1. 리더보드 필터 UI를 캐릭터/지역/플랫폼까지 확장합니다.
2. Wavu 검색 cache를 장기적으로 별도 search snapshot 테이블로 분리할지 검토합니다.
3. 더 보기/필터/추천 검색/리더보드 상태에 대한 프론트 테스트 전략을 정합니다.

## Product Direction

T8LAB starts with EWGF Pro API-backed player lookup, recent match history, and basic player analysis. The project is structured to add PostgreSQL-backed caching, leaderboards, Wavu data, and richer statistics over time.

See:

- `docs/product-requirements.md`
- `docs/architecture.md`
- `docs/wavu-datasource-plan.md`

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
WAVU_BASE_URL=https://wank.wavu.wiki
WAVU_ENABLED=true
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server: `http://localhost:5173`
