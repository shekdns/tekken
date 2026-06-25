# Leaderboard and Search History Plan

## 목적

T8LAB의 다음 확장 축은 플레이어 상세 페이지를 넘어, 사용자가 탐색할 대상을 먼저 발견할 수 있게 만드는 것입니다.

이 문서는 리더보드와 검색 이력 기반 추천 검색의 1차 설계를 정리합니다.

## 설계 원칙

1. 검색 이력 기반 추천은 현재 DB 구조로 바로 시작합니다.
2. 리더보드는 EWGF만으로 즉시 완성하기 어렵기 때문에 snapshot 구조를 먼저 준비합니다.
3. Wavu 연동이 붙어도 프론트 API 계약은 크게 바꾸지 않도록 service-owned DTO를 둡니다.
4. 개인화 기능은 로그인 전까지 브라우저 로컬 상태와 서버 전체 검색 이력을 분리해서 다룹니다.
5. 공개 리더보드는 freshness, source, calculatedAt을 함께 표시합니다.

## 검색 이력 기반 추천 검색

### 현재 상태

이미 존재하는 테이블:

```text
player_search_history
```

현재 필드:

```text
id
query
tekken_id
searched_at
```

현재 backend repository:

```text
findTop20ByOrderBySearchedAtDesc()
```

### 1차 목표

검색창 아래 또는 검색 전 empty state에 최근/인기 검색을 노출합니다.

추천 검색은 두 종류로 나눕니다.

1. 최근 검색
   - 서버 기준 최근 검색된 Tekken ID 목록.
   - 중복 Tekken ID는 최신 1개만 노출.
   - MVP에서는 전체 사용자 기준으로 시작합니다.

2. 인기 검색
   - 일정 기간 안에 많이 검색된 Tekken ID 목록.
   - 초기 기준은 최근 7일 또는 최근 30일.
   - 같은 Tekken ID 검색 횟수 기준으로 정렬합니다.

### API 초안

```http
GET /api/search/recent?limit=10
GET /api/search/popular?days=7&limit=10
```

응답 예시:

```json
{
  "items": [
    {
      "tekkenId": "27tB4yhFmfNE",
      "displayTekkenId": "27tB-4yhF-mfNE",
      "query": "27tB-4yhF-mfNE",
      "searchCount": 12,
      "lastSearchedAt": "2026-06-25T10:30:00Z"
    }
  ]
}
```

### Backend 패키지 방향

```text
com.project.tekken.search
  SearchController
  SearchSuggestionService
  SearchSuggestionResponse
  SearchSuggestionItem
```

기존 `PlayerSearchHistoryEntity`, `PlayerSearchHistoryRepository`는 유지합니다.

### Repository 확장 방향

최근 검색:

```text
findTopNByOrderBySearchedAtDesc
```

인기 검색:

```text
searched_at >= from
group by tekken_id
order by count(*) desc, max(searched_at) desc
```

JPA projection 또는 native query를 사용할 수 있습니다.

### 프론트 UI 위치

1차 위치:

- 검색 폼 아래.
- 검색 결과가 없을 때 empty panel 안쪽.

UI 구성:

- `최근 검색` 탭
- `인기 검색` 탭
- 각 항목 클릭 시 `/players/{tekkenId}`로 이동 또는 검색 실행

## 리더보드

### 문제 정의

리더보드는 단순히 현재 검색된 플레이어 목록을 정렬하는 것과 다릅니다.

필요한 것:

- 충분한 플레이어 표본
- 랭크/점수/캐릭터/지역/플랫폼 기준
- 정기 갱신 또는 외부 데이터 소스
- freshness 표시

EWGF profile/matches만으로는 전역 리더보드를 완전하게 만들기 어렵습니다. 따라서 1차는 `검색된 플레이어 기반 리더보드`, 이후 Wavu 연동 후 `외부 순위 기반 리더보드`로 확장합니다.

### 1차 리더보드 범위

검색/갱신된 플레이어 중 DB에 저장된 profile snapshot 기준으로 정렬합니다.

초기 정렬 후보:

- Tekken Prowess 높은 순
- 최근 갱신 시각 최신 순
- 검색 횟수 높은 순
- 캐릭터별 검색/대표 플레이어

초기 필터 후보:

- 캐릭터
- 지역
- 플랫폼
- 랭크

### API 초안

```http
GET /api/leaderboards/players?sort=prowess&character=Dragunov&region=KR&limit=50
GET /api/leaderboards/searches?days=7&limit=50
```

응답 예시:

```json
{
  "source": "t8lab",
  "calculatedAt": "2026-06-25T10:30:00Z",
  "filters": {
    "sort": "prowess",
    "character": "Dragunov",
    "region": "KR",
    "limit": 50
  },
  "items": [
    {
      "rank": 1,
      "tekkenId": "27tB4yhFmfNE",
      "name": "Player",
      "mainCharacter": "Dragunov",
      "danRank": "God of Destruction",
      "tekkenProwess": 369393,
      "region": "KR",
      "platform": "Steam",
      "lastUpdatedAt": "2026-06-25T10:20:00Z"
    }
  ]
}
```

### Backend 패키지 방향

```text
com.project.tekken.leaderboard
  LeaderboardController
  LeaderboardService
  PlayerLeaderboardResponse
  PlayerLeaderboardItem
  LeaderboardFilters
```

### DB 방향

1차는 기존 `players` 테이블과 `player_search_history` 테이블을 활용합니다.

추후 snapshot 안정화가 필요하면 아래 테이블을 추가합니다.

```text
player_leaderboard_snapshots
```

후보 필드:

```text
id
source
tekken_id
name
main_character
dan_rank
tekken_prowess
region
platform
rank_position
snapshot_at
raw
```

## 단계별 작업 순서

### Phase 1: 추천 검색

1. `GET /api/search/recent` 추가. 완료
2. `GET /api/search/popular` 추가. 완료
3. 중복 Tekken ID 제거와 limit 검증 테스트 추가. 완료
4. 검색 화면에 최근/인기 검색 UI 추가. 완료

### Phase 2: T8LAB 내부 리더보드

1. `players` 저장 데이터 기준 player leaderboard DTO 설계. 완료
2. `GET /api/leaderboards/players` 추가. 완료
3. sort/filter 계약 추가. 완료
4. 프론트에 리더보드 페이지 또는 검색 홈 섹션 추가. 완료

### Phase 3: 외부 소스 확장

1. Wavu datasource 경계 설계.
2. 외부 leaderboard snapshot 저장.
3. source별 freshness 표시.
4. 리더보드 화면에서 source와 갱신 시각 표시.

## 현재 결정

현재 구현 우선순위는 `Wavu datasource 경계 설계` 또는 `닉네임/Tekken ID 자동완성 검색 설계`입니다.

이유:

- 추천 검색 API와 검색 홈 UI 연결은 1차 완료되었습니다.
- 내부 리더보드 API와 검색 홈 UI 연결은 1차 완료되었습니다.
- 검색 이력 데이터는 이후 내부 리더보드의 seed 데이터로 활용할 수 있습니다.
- Wavu 연동 경계를 먼저 잡아두면 외부 데이터 소스를 추가할 때 프론트 계약을 안정적으로 유지할 수 있습니다.
