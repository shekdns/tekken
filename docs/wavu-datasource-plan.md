# Wavu Datasource Plan

## 목적

Wavu는 T8LAB에서 EWGF만으로 부족한 외부 플레이어/리더보드 데이터를 보강하기 위한 후보 데이터 소스입니다.

다만 Wavu의 실제 호출 계약은 별도로 검증해야 하므로, 1차에서는 원시 응답을 서비스 API로 바로 노출하지 않습니다. 대신 `datasource.wavu` 경계를 먼저 만들고, 이후 확인된 데이터를 T8LAB 전용 DTO로 변환합니다.

## 현재 결정

1. Wavu 코드는 `com.project.tekken.datasource.wavu` 패키지에 둡니다.
2. 자동완성 검색에서는 기본 활성화합니다.
3. Wavu 응답은 프론트에 직접 노출하지 않습니다.
4. 검증된 응답만 `leaderboard`, `search`, `player` 서비스 DTO로 변환합니다.
5. 실제 서버 배포 전에는 rate limit, 캐싱, robots/이용 정책을 확인합니다.
6. 플레이어 검색은 Wavu 화면 endpoint인 `/player/search?q={query}`의 HTML 응답을 backend에서 파싱해 사용합니다.
7. `_format=json` 형태는 검색 endpoint에서 JSON 응답으로 확인되지 않았으므로 현재는 사용하지 않습니다.

## 설정

```yaml
wavu:
  base-url: ${WAVU_BASE_URL:https://wank.wavu.wiki}
  enabled: ${WAVU_ENABLED:true}
```

환경변수:

```text
WAVU_BASE_URL
WAVU_ENABLED
```

`WAVU_ENABLED`를 지정하지 않으면 기본값은 `true`입니다. Wavu 장애나 운영 정책상 차단이 필요할 때만 명시적으로 `false`를 지정합니다.

## 패키지 구조

```text
com.project.tekken.datasource.wavu
  WavuDataSourceProperties
  WavuDataSourceClient
  WavuDataSourceService
  WavuPlayerSearchResult
```

## 연동 원칙

Wavu 연동은 아래 순서로 진행합니다.

1. 실제 사용 가능한 Wavu endpoint 확인.
2. 원시 응답 샘플을 문서화.
3. T8LAB 내부 DTO로 변환할 mapper 작성.
4. `api_cache` 또는 별도 snapshot 테이블에 캐싱.
5. 검색 자동완성 또는 리더보드에서 datasource 결과를 병합.

현재 플레이어 자동완성은 1, 3, 5번을 적용했고, Wavu 검색 결과는 `api_cache`에 10분 TTL로 저장합니다.
장기적으로 검색 품질 개선이나 랭킹/추천에 재사용할 필요가 커지면 별도 검색 snapshot 테이블로 분리합니다.

## 우선 적용 후보

### 플레이어 자동완성

```http
GET /api/search/players?q={query}&limit=10
```

초기 데이터 소스:

1. T8LAB `players` 테이블
2. T8LAB `player_search_history` 테이블
3. Wavu `/player/search` HTML 파싱 결과

Wavu에서 확인한 필드:

- player page href 기반 내부 Tekken ID
- 화면용 Tekken ID
- 닉네임
- 플랫폼 title

캐시 정책:

- cache key: `wavu:player-search:{normalizedQuery}`
- source: `wavu`
- TTL: 10분
- 저장 위치: `api_cache.response_json.items`
- raw HTML은 저장하지 않고 T8LAB 내부 검색 후보 DTO에 필요한 필드만 저장합니다.

### 외부 리더보드 snapshot

```http
GET /api/leaderboards/players?source=wavu&sort=rank
```

초기에는 `source=t8lab` 리더보드와 분리합니다. Wavu 데이터 품질과 갱신 주기를 확인한 뒤 통합 리더보드로 확장합니다.

## 아직 하지 않는 것

- Wavu raw proxy API 공개
- 프론트에서 Wavu 직접 호출
- 확인되지 않은 HTML/JSON 구조를 프론트 계약으로 노출
- 로그인/개인화 기반 추천
