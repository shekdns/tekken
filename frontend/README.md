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

### 현재 단계

- 플레이어 검색, 프로필 요약, 최근 경기 목록, 최근 경기 기반 통계 표시는 완료된 상태입니다.
- 아직 화면 구조는 초기 기능 검증용이며, 디자인은 추후 TKNOW.GG/EWGF.GG를 참고해 고도화합니다.

### 다음 작업

1. `/players/{tekkenId}` 상세 페이지 라우팅 구조로 분리합니다.
2. 검색 후 URL이 바뀌고 상세 화면으로 이동하는 흐름을 만듭니다.
3. 통계 계산 로직을 추후 backend API로 이전할 수 있도록 응답 계약을 정합니다.
4. 전적 사이트다운 상세 화면 디자인을 고도화합니다.

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
GET /api/players/{tekkenId}
GET /api/players/{tekkenId}/matches
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
8. `/players/{tekkenId}` 라우트를 추가합니다. `다음 작업`

## 8. OS별 실행 명령

`npm` 명령은 macOS/Linux와 Windows PowerShell에서 동일하게 사용합니다.

```bash
npm install
npm run dev
npm run build
```
