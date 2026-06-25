# T8LAB Frontend

T8LAB의 프론트엔드 프로젝트입니다. React + Vite 기반으로 철권8 플레이어 검색, 전적 조회, 기본 분석 화면을 제공합니다.

현재 프론트엔드는 검색 중심 플레이어 조회 화면을 제공합니다. 다음 목표는 검색 결과 아래에 최근 경기 목록을 표시하는 것입니다.

## 현재 진행 상황

이 섹션은 프론트엔드 단계가 끝날 때마다 업데이트합니다.

### 완료

1. Vite + React 기반 프론트엔드 프로젝트를 구성했습니다.
2. Vite dev server에서 `/api` 요청을 Spring Boot backend로 프록시하도록 설정했습니다.
3. 기존 헬스체크 화면을 플레이어 검색 화면으로 교체했습니다.
4. Tekken ID 검색 폼을 추가했습니다.
5. 샘플 ID 입력 버튼을 추가했습니다.
6. `GET /api/players/{tekkenId}`를 호출해 플레이어 정보를 표시합니다.
7. backend online/offline 상태를 화면 상단에 표시합니다.
8. 플레이어 요약 카드에 다음 정보를 표시합니다.
   - 이름
   - 랭크
   - 주 캐릭터
   - 지역
   - Prowess
   - 플랫폼
   - 조회 시각
9. EWGF 원본 응답을 확인할 수 있는 `원본 응답 보기` 영역을 추가했습니다.
10. 샘플 플레이어 기준 `God of Destruction`, `Dragunov`, `369,393` 표시를 확인했습니다.
11. `GET /api/players/{tekkenId}/matches`를 호출해 최근 경기 목록을 표시합니다.
12. 최근 경기에서 승/패, 경기 타입, 내 캐릭터, 상대 캐릭터, 상대 이름, 상대 랭크, 라운드 스코어, 경기 시간을 표시합니다.
13. backend 재시작 전 raw matches 응답이 와도 화면에서 표시할 수 있는 fallback 정규화를 추가했습니다.
14. 최근 경기의 양쪽 플레이어 정보를 `캐릭터`, `닉네임`, `계급` 순서로 표시하도록 정리했습니다.
15. `main.jsx`에 모여 있던 코드를 `app`, `features`, `shared` 구조로 분리했습니다.
16. 최근 100게임 기준 통계 패널을 추가했습니다.
17. 전적, 승률, 최근 10게임, 최다 사용 캐릭터, 캐릭터별 사용률과 승률을 표시합니다.
18. `/players/{tekkenId}` 상세 URL 라우팅을 추가했습니다.
19. 검색 성공 시 상세 URL로 이동하고, 상세 URL 직접 접근 시 데이터를 자동 조회합니다.
20. 로고를 누르면 홈(`/`) 상태로 돌아가도록 정리했습니다.
21. `GET /api/players/{tekkenId}/stats` backend 통계 API를 우선 사용하도록 변경했습니다.
22. stats API 실패 시 기존 matches 기반 프론트 계산으로 fallback합니다.
23. battle type별 승률과 상대 캐릭터별 전적을 통계 패널에 표시합니다.
24. 최근 경기 목록에 backend `offset/limit` 기반 `더 보기` 버튼을 연결했습니다.
25. 최근 경기 API 호출부에서 `battleType`, `character`, `opponentCharacter`, `days` 필터 파라미터를 보낼 수 있게 준비했습니다.
26. 프로필 아래에 공통 전적 필터 UI를 추가했습니다.
27. 필터 적용 시 stats API와 matches API를 같은 기준으로 다시 조회합니다.
28. 필터 변경 후 최근 경기 목록은 `offset=0`부터 다시 조회하고, 더 보기는 현재 필터 기준을 유지합니다.
29. 최근 경기 패널에 갱신 버튼을 추가하고 `refresh=true`로 matches API를 호출하도록 연결했습니다.
30. `GET /api/characters/options`를 호출해 캐릭터 필터를 선택형으로 표시합니다.
31. 갱신 버튼이 프로필과 최근 경기/통계를 함께 새로 가져오도록 전체 갱신 흐름으로 변경됐습니다.
32. 캐릭터 선택지는 한글명과 영문명을 함께 표시합니다.
33. 캐릭터 선택지는 `localizedNames`와 현재 locale을 기준으로 표시합니다.
34. 캐릭터 초상화는 `/assets/characters/{assetKey}.webp` 규칙을 사용하고, 이미지가 없으면 텍스트 fallback을 표시합니다.
35. 플레이어 상세 화면을 프로필/필터 사이드바와 통계/최근 경기 본문으로 나누는 1차 대시보드 레이아웃으로 정리했습니다.
36. 전체 갱신 버튼을 최근 경기 패널에서 프로필 카드 상단의 `전적 갱신` 버튼으로 이동했습니다.
37. 랭크와 전투 타입 표시명을 한국어(`ko`), 영어(`en`), 일본어(`ja`) locale 메타데이터 기반으로 표시하도록 분리했습니다.
38. 주요 UI 문구를 한국어(`ko`), 영어(`en`), 일본어(`ja`) 리소스로 분리하고 상단 언어 선택 UI를 추가했습니다.
39. 캐릭터 이미지 자산 정책을 문서화하고, 권리 확인 전에는 텍스트/추상 fallback을 유지하기로 정리했습니다.
40. 캐릭터 이름/assetKey 기반 색상과 패턴을 사용하는 T8LAB 전용 초상화 placeholder UI를 추가했습니다.
41. 프로필 주캐릭터 spotlight, 승률 강조 카드, 최근 경기 결과 배지를 추가해 상세 화면을 전적 사이트 톤으로 2차 고도화했습니다.
42. stats API의 `streakStats`, `activityStats`를 통계 패널 카드로 연결했습니다.
43. API 오류 `code`를 현재 locale의 오류 문구로 변환해 표시하도록 정리했습니다.
44. 리더보드와 검색 이력 기반 추천 검색 UI/API 방향을 문서화했습니다.
45. `GET /api/search/recent`, `GET /api/search/popular`를 호출하는 추천 검색 API 클라이언트를 추가했습니다.
46. 검색 카드에 최근 검색/인기 검색 추천 UI를 연결하고, 항목 클릭 시 플레이어 상세 조회로 이동하도록 정리했습니다.
47. 추천 검색 UI 문구를 한국어(`ko`), 영어(`en`), 일본어(`ja`) 리소스에 추가했습니다.
48. `GET /api/leaderboards/players`를 호출하는 리더보드 API 클라이언트를 추가했습니다.
49. 검색 화면 아래에 내부 플레이어 리더보드 UI를 연결했습니다.
    - Prowess
    - 최근 갱신
    - 검색 인기
50. 리더보드 항목 클릭 시 해당 플레이어 상세 조회로 이동하도록 연결했습니다.

### 현재 단계

- 플레이어 검색, 프로필 요약, 추천 검색 UI, 내부 리더보드 UI, 공통 전적 필터, locale 기반 캐릭터 선택형 필터, 랭크/전투 타입 locale 표시, 주요 UI 문구 i18n 리소스와 언어 선택 UI, locale 기반 API 오류 표시, 캐릭터 초상화 fallback과 자산 정책, T8LAB 전용 초상화 placeholder, 최근 경기 더보기/전체 갱신 목록, backend 통계 API 연동, streak/activity 확장 통계 표시, 상세 URL 라우팅, 대시보드 레이아웃, 프로필 상단 전적 갱신 액션, 상세 화면 2차 디자인 고도화, 리더보드/추천 검색 설계는 완료된 상태입니다.
- 아직 전체 디자인은 초기 구축 단계이며, 추후 TKNOW.GG/EWGF.GG를 참고해 추가 고도화합니다.

### 다음 작업

1. 리더보드 필터 UI를 캐릭터/지역/플랫폼까지 확장합니다.
2. 닉네임/Tekken ID 자동완성 검색 UI 방향을 정합니다.
3. 더 보기/필터/추천 검색/리더보드 상태에 대한 프론트 테스트 전략을 정합니다.

## 캐릭터 이미지 자산 규칙

캐릭터 이미지는 아래 경로 규칙을 사용합니다.

```text
frontend/public/assets/characters/{assetKey}.webp
```

예시:

```text
frontend/public/assets/characters/dragunov.webp
frontend/public/assets/characters/kazuya.webp
frontend/public/assets/characters/reina.webp
```

이미지가 없으면 화면에서는 텍스트 fallback을 표시합니다.

## 1. 사전 준비

- Node.js 20 이상
- 백엔드 서버 실행 상태: `http://localhost:8080`

Node.js 버전 확인:

```bash
node --version
npm --version
```

## 2. 의존성 설치

`frontend` 디렉터리에서 실행합니다.

```bash
npm install
```

## 3. 로컬 실행

```bash
npm run dev
```

Vite 개발 서버는 기본적으로 아래 주소에서 실행됩니다.

```text
http://localhost:5173
```

현재 개발 서버는 `--host 0.0.0.0` 옵션으로 실행되므로, 같은 네트워크의 다른 기기에서도 접근할 수 있습니다.

## 4. 빌드

```bash
npm run build
```

프로덕션 빌드 결과를 미리 확인하려면 다음 명령을 사용합니다.

```bash
npm run preview
```

## 5. 백엔드 의존성

프론트엔드는 EWGF API를 직접 호출하지 않습니다. 모든 외부 API 호출은 Spring Boot 백엔드를 통해 처리합니다.

현재 백엔드 상태 확인 API:

```http
GET /api/health
```

현재 EWGF 프록시 API:

```http
GET /api/ewgf/battles/{tekkenId}
GET /api/ewgf/profile/{tekkenId}
POST /api/ewgf/profile
```

현재 또는 추후 프론트엔드에서 사용할 서비스 전용 API:

```http
GET /api/search/recent
GET /api/search/popular
GET /api/leaderboards/players
GET /api/players/{tekkenId}
GET /api/players/{tekkenId}/matches
GET /api/players/{tekkenId}/stats
GET /api/players/{tekkenId}/summary
GET /api/players/{tekkenId}/character-stats
```

## 6. 계획된 소스 구조

현재 소스는 아래 구조로 확장되고 있습니다.

```text
frontend/src
  app
    App.jsx
    router.jsx
    providers.jsx

  pages
    HomePage.jsx
    PlayerPage.jsx
    AboutPage.jsx

  features
    player-search
    player-profile
    match-history
    character-stats
    opponent-stats
    leaderboard

  shared
    api
      playerApi.js
    components
    hooks
    styles
    utils
```

## 7. MVP 프론트엔드 작업 순서

1. 현재 헬스체크 화면을 검색 중심 홈 화면으로 교체합니다. `완료`
2. Tekken ID 검색 폼을 추가합니다. `완료`
3. `/api/players/{tekkenId}` 응답으로 플레이어 요약 정보를 렌더링합니다. `완료`
4. `shared/api` 아래에 백엔드 API 클라이언트를 추가합니다.
5. `PlayerPage` 라우트를 추가합니다.
6. `/api/players/{tekkenId}/matches` 응답으로 최근 경기 목록을 렌더링합니다. `완료`
7. 캐릭터 사용률과 승률 패널을 추가합니다. `완료`
8. `/players/{tekkenId}` 라우트를 추가합니다. `완료`
9. 상세 화면 디자인을 고도화합니다. `다음 작업`

## 8. OS별 실행 명령

`npm` 명령은 macOS/Linux와 Windows PowerShell에서 동일하게 사용합니다.

```bash
npm install
npm run dev
npm run build
```
