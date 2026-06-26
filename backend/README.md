# T8LAB Backend

T8LAB의 백엔드 프로젝트입니다. Spring Boot 기반으로 철권8 플레이어 조회, EWGF API 프록시, PostgreSQL 기반 캐싱, 향후 통계 기능을 담당합니다.

현재 백엔드는 EWGF 프록시 API와 T8LAB 서비스 전용 플레이어 API를 제공합니다. 다음 목표는 매치 히스토리 응답을 표준 DTO로 정리하고 프론트엔드에서 바로 사용할 수 있게 만드는 것입니다.

## 현재 진행 상황

이 섹션은 백엔드 단계가 끝날 때마다 업데이트합니다.

### 완료

1. EWGF API key를 `EWGF_API_KEY` 환경변수에서 읽도록 구성했습니다.
2. PostgreSQL 연결, JPA, Flyway 구성을 추가했습니다.
3. 초기 DB 테이블을 추가했습니다.
   - `players`
   - `matches`
   - `api_cache`
   - `player_search_history`
4. EWGF API 프록시를 유지했습니다.
   - `GET /api/ewgf/battles/{tekkenId}`
   - `GET /api/ewgf/profile/{tekkenId}`
   - `POST /api/ewgf/profile`
5. T8LAB 서비스 전용 플레이어 API를 추가했습니다.
   - `GET /api/players/{tekkenId}`
   - `GET /api/players/{tekkenId}/matches`
6. 플레이어 검색 시 검색 이력을 저장합니다.
7. EWGF profile/battles 응답을 `api_cache`에 저장합니다.
8. EWGF profile 응답을 `PlayerProfileSummary`로 표준화했습니다.
9. `main_character`가 `{ "Dragunov": "God of Destruction" }` 형태로 오는 경우 캐릭터와 랭크를 분리해서 매핑합니다.
10. EWGF battles 응답을 `PlayerMatchSummary`로 표준화했습니다.
11. match 응답에서 검색 대상 플레이어를 `my`, 상대를 `opponent`로 분리합니다.
12. `winner`와 검색 대상 side를 기준으로 `WIN`/`LOSS`를 계산합니다.
13. 최근 경기 기반 `GET /api/players/{tekkenId}/stats` 통계 API를 추가했습니다.
14. stats 응답에 전적, 승률, 최근 10게임, 최다 사용 캐릭터, 캐릭터별 사용/승률을 포함합니다.
15. stats API에 `limit`, `battleType` query parameter를 추가했습니다.
16. stats 응답에 battle type별 통계와 상대 캐릭터별 전적을 포함합니다.
17. stats API에 `character`, `opponentCharacter`, `days` query parameter를 추가했습니다.
18. matches API에 `offset`, `limit`, `total`, `hasMore`, `nextOffset` 기반 더보기 계약을 추가했습니다.
19. matches API에 `battleType`, `character`, `opponentCharacter`, `days` query parameter를 추가했습니다.
20. matches API의 `total`, `hasMore`, `nextOffset`은 필터 적용 후 목록 기준으로 계산합니다.
21. stats API의 battle type별 통계도 필터 적용 후 목록 기준으로 계산하도록 정리했습니다.
22. `PlayerStatsCalculator` 단위 테스트를 추가했습니다.
23. `PlayerMatchMapper` 단위 테스트를 추가했습니다.
24. `PlayerProfileMapper` 단위 테스트를 추가했습니다.
25. `PlayerService` 단위 테스트를 추가했습니다.
    - profile 캐시 hit/miss
    - matches 캐시 hit/miss
    - matches 필터/페이징
    - stats 계산 연결
    - EWGF 오류/invalid JSON 처리
26. EWGF 외부 API 계층을 `datasource.ewgf` 패키지로 이동했습니다.
27. EWGF battles 호출 실패 시 DB에 저장된 match 데이터로 fallback하도록 추가했습니다.
28. `MatchEntity`에서 `PlayerMatchSummary`로 복원하는 매핑 테스트를 추가했습니다.
29. match cache/API/DB fallback/저장 책임을 `PlayerMatchSyncService`로 분리했습니다.
30. `player` 하위 코드를 역할별 패키지로 분리했습니다.
    - `player.controller`
    - `player.service`
    - `player.dto`
    - `player.mapper`
    - `player.stats`
    - `player.exception`
31. DB match 조회/fallback 변환 책임을 `PlayerMatchQueryService`로 분리했습니다.
32. fresh cache가 없더라도 DB match 데이터가 최근 10분 이내면 EWGF 호출 없이 DB를 우선 사용하도록 정책을 추가했습니다.
33. matches API에 `refresh=true` query parameter를 추가해 fresh cache/DB를 건너뛰고 EWGF 데이터를 강제로 갱신할 수 있게 했습니다.
34. `matches` 테이블 기준으로 `offset`, `limit`, `battleType`, `character`, `opponentCharacter`, `days` 필터를 적용하는 DB 조회 구조를 추가했습니다.
35. stats API가 저장된 `matches` 테이블을 우선 사용해 승률, 최근 10게임, 캐릭터별 통계, 상대 캐릭터별 전적을 계산하도록 확장했습니다.
36. `GET /api/characters/options` 캐릭터 옵션 API를 추가했습니다.
37. `GET /api/players/{tekkenId}?refresh=true` 프로필 강제 갱신 옵션을 추가했습니다.
38. 캐릭터 옵션 응답에 `localizedNames`, `assetKey`, `aliases`, `imageUrl` 메타데이터 필드를 정리했습니다.
39. 캐릭터명은 한국어(`ko`), 영어(`en`), 일본어(`ja`) 기준으로 제공합니다.
40. `assetKey`는 프론트 정적 자산 경로 `/assets/characters/{assetKey}.webp`와 연결할 수 있게 유지합니다.
41. stats API 응답에 `streakStats`를 추가해 현재 연승/연패, 현재 streak 수, 최장 연승, 최장 연패를 계산합니다.
42. stats API 응답에 `activityStats`를 추가해 첫 경기 시각, 최근 경기 시각, 활동일 수를 계산합니다.
43. `PlayerApiErrorResponse`에 `code`를 추가해 프론트가 locale 기반 오류 문구로 표시할 수 있게 했습니다.
44. 검색 이력 기반 추천 검색 API를 추가했습니다.
    - `GET /api/search/recent`
    - `GET /api/search/popular`
45. 추천 검색 API의 최근 검색 중복 제거, limit 보정, 인기 검색 응답 매핑 테스트를 추가했습니다.
46. T8LAB 내부 플레이어 리더보드 API를 추가했습니다.
    - `GET /api/leaderboards/players`
    - `sort=prowess|recent|searches`
    - `character`, `region`, `platform`, `limit` 필터
47. 리더보드 서비스 테스트를 추가해 prowess 정렬, 검색 횟수 정렬, 필터 적용을 검증했습니다.
48. Wavu 연동을 위한 `datasource.wavu` 패키지 경계를 추가했습니다.
    - `WavuDataSourceProperties`
    - `WavuDataSourceClient`
    - `WavuDataSourceService`
49. Wavu 자동완성은 기본 활성화 상태로 두고, 검증된 응답만 T8LAB DTO로 변환하기로 정리했습니다.
50. 닉네임/Tekken ID 자동완성 검색 API 방향을 문서화했습니다.
51. 닉네임/Tekken ID 자동완성 검색 backend API를 추가했습니다.
    - `GET /api/search/players?q={query}&limit=10`
52. 자동완성 검색 서비스 테스트를 추가해 짧은 검색어, players 우선순위, search history fallback, limit 보정을 검증했습니다.
53. Wavu `/player/search?q={query}` 화면 endpoint를 확인하고 HTML 응답을 파싱해 자동완성 후보에 병합했습니다.
54. Wavu 검색 파싱과 자동완성 Wavu fallback 테스트를 추가했습니다.
55. Wavu 자동완성 결과를 `api_cache`에 10분 TTL로 저장하도록 캐싱을 추가했습니다.
56. `WAVU_ENABLED=true` 상태에서 실제 `/api/search/players?q=lowhigh&limit=5` 응답과 cache 저장을 확인했습니다.

### 현재 단계

- 플레이어 프로필 요약 API, 캐릭터 옵션/메타데이터 API, 매치 히스토리 표준 응답/더보기/필터/전체 갱신, DB 기준 match 조회, DB 기준 stats API 계산, streak/activity 확장 통계, locale 대응 가능 오류 코드, 추천 검색 API, 자동완성 검색 API, 내부 플레이어 리더보드 API, Wavu datasource 경계, Wavu 검색 후보 병합, stats/match/profile/search/leaderboard mapper-service 테스트, service 테스트, match sync/query 분리, 역할별 패키지 분리, DB match fresh 우선 사용/fallback은 1차 구현 완료 상태입니다.
- 리더보드와 검색 이력 기반 추천 검색의 API/DB/UI 확장 방향은 `docs/leaderboard-search-history.md`에 정리했고, Wavu 연동 방향은 `docs/wavu-datasource-plan.md`에 정리했습니다.
- 다음 단계는 리더보드 필터 UI 확장 또는 Wavu 검색 cache의 별도 snapshot 테이블 분리 검토입니다.

### 다음 작업

1. 리더보드 필터 UI를 확장합니다.
2. Wavu 검색 cache를 장기적으로 별도 search snapshot 테이블로 분리할지 검토합니다.
3. 자동완성/추천 검색 API 테스트를 보강합니다.

## 1. 사전 준비

- Java 17
- Docker Desktop
- `EWGF_API_KEY` 환경변수

Java 버전 확인:

```bash
java -version
```

## 2. PostgreSQL 실행

저장소 루트에서 실행합니다.

```bash
docker compose up -d
```

기본 데이터베이스 설정:

```text
DB_URL=jdbc:postgresql://localhost:5432/tekken
DB_USERNAME=tekken
DB_PASSWORD=tekken
```

백엔드는 위 값을 환경변수에서 읽습니다. 별도 설정이 없으면 `application.yml`에 정의된 기본값을 사용합니다.

## 3. 환경변수 설정

필수 환경변수:

```text
EWGF_API_KEY
```

선택 환경변수:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
EWGF_SAMPLE_TEKKEN_ID
EWGF_ENABLE_PROFILE_TESTS
WAVU_BASE_URL
WAVU_ENABLED
```

macOS/Linux:

```bash
export EWGF_API_KEY=replace_with_your_ewgf_api_key
export DB_URL=jdbc:postgresql://localhost:5432/tekken
export DB_USERNAME=tekken
export DB_PASSWORD=tekken
export WAVU_ENABLED=true
```

Windows PowerShell:

```powershell
$env:EWGF_API_KEY="replace_with_your_ewgf_api_key"
$env:DB_URL="jdbc:postgresql://localhost:5432/tekken"
$env:DB_USERNAME="tekken"
$env:DB_PASSWORD="tekken"
$env:WAVU_ENABLED="true"
```

실제 API key와 비밀번호는 커밋하지 않습니다.

## 4. 백엔드 실행

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

상태 확인:

```http
GET http://localhost:8080/api/health
```

## 5. 테스트 실행

macOS/Linux:

```bash
sh gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

기본 컨텍스트 테스트는 `test` 프로필과 H2 인메모리 DB를 사용하므로 로컬 PostgreSQL이 없어도 실행됩니다.

EWGF 연동 테스트는 `EWGF_API_KEY`가 설정된 경우에만 실행됩니다.

Profile API 연동 테스트는 아래 환경변수가 추가로 설정된 경우에만 실행됩니다.

```bash
export EWGF_ENABLE_PROFILE_TESTS=true
```

## 6. 현재 API

프론트엔드는 EWGF API를 직접 호출하지 않고, 백엔드를 통해 호출해야 합니다.

```http
GET /api/health
GET /api/ewgf/battles/{tekkenId}
GET /api/ewgf/profile/{tekkenId}
POST /api/ewgf/profile
GET /api/characters/options
GET /api/search/recent
GET /api/search/recent?limit=10
GET /api/search/popular
GET /api/search/popular?days=7&limit=10
GET /api/search/players?q=player&limit=10
GET /api/leaderboards/players
GET /api/leaderboards/players?sort=prowess&limit=50
GET /api/leaderboards/players?sort=recent&region=KR
GET /api/leaderboards/players?sort=searches&character=Dragunov&platform=Steam
GET /api/players/{tekkenId}
GET /api/players/{tekkenId}?refresh=true
GET /api/players/{tekkenId}/matches
GET /api/players/{tekkenId}/matches?offset=12&limit=12
GET /api/players/{tekkenId}/matches?battleType=RANKED_BATTLE
GET /api/players/{tekkenId}/matches?character=Kazuya
GET /api/players/{tekkenId}/matches?opponentCharacter=Bryan
GET /api/players/{tekkenId}/matches?days=30
GET /api/players/{tekkenId}/matches?refresh=true
GET /api/players/{tekkenId}/stats
GET /api/players/{tekkenId}/stats?limit=20
GET /api/players/{tekkenId}/stats?battleType=RANKED_BATTLE
GET /api/players/{tekkenId}/stats?character=Kazuya
GET /api/players/{tekkenId}/stats?opponentCharacter=Bryan
GET /api/players/{tekkenId}/stats?days=30
```

Wavu datasource는 현재 backend 내부 경계만 준비되어 있으며, raw proxy API는 공개하지 않습니다.
Wavu `/player/search` 결과는 backend 내부에서만 파싱해 자동완성 후보로 병합하며, raw HTML은 프론트에 노출하지 않습니다.

`POST /api/ewgf/profile` 요청 본문:

```json
["27tB-4yhF-mfNE"]
```

## 7. 예정된 서비스 전용 API

장기적으로 EWGF 응답 구조를 프론트엔드에 직접 노출하지 않습니다. 백엔드에서 외부 데이터를 T8LAB 전용 DTO로 변환합니다.

예정 API:

```http
GET /api/players/{tekkenId}/summary
GET /api/players/{tekkenId}/character-stats
```

## 8. 데이터베이스 마이그레이션

Flyway 마이그레이션 파일 위치:

```text
backend/src/main/resources/db/migration
```

초기 마이그레이션:

```text
V1__init_core_tables.sql
```

초기 테이블:

- `players`
- `matches`
- `api_cache`
- `player_search_history`

## 9. 계획된 패키지 구조

현재 EWGF 외부 API 코드는 `datasource.ewgf` 패키지로 정리했습니다. 이후 Wavu 연동 시 같은 방식으로 `datasource.wavu`를 추가합니다.

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
    controller
    service
    dto
    mapper
    stats
    exception
  match
  stats
  leaderboard
  cache
```

## 10. 백엔드 구현 순서

1. 기존 EWGF 프록시 API를 안정적으로 유지합니다. `완료`
2. 초기 테이블에 대응하는 JPA Entity와 Repository를 추가합니다. `완료`
3. EWGF Profile API와 DB 캐시를 사용하는 `player` API를 추가합니다. `완료`
4. EWGF profile 응답을 T8LAB 표준 summary DTO로 변환합니다. `완료`
5. EWGF Battles API와 DB 캐시를 사용하는 `match` API를 추가합니다. `완료`
6. 기본 승률, 캐릭터별 사용률, 캐릭터별 승률을 계산하는 `stats` 서비스를 추가합니다. `완료`
7. `datasource.ewgf` 패키지를 만들고 EWGF 클라이언트 코드를 이동합니다. `완료`
8. 프론트엔드 호출을 `/api/ewgf/*`에서 `/api/players/*`로 전환합니다. `일부 완료`
