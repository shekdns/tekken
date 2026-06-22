# T8LAB Frontend

T8LAB의 프론트엔드 프로젝트입니다. React + Vite 기반으로 철권8 플레이어 검색, 전적 조회, 기본 분석 화면을 제공합니다.

현재 프론트엔드는 아직 스타터 단계입니다. 1차 목표는 백엔드 API만 호출하는 검색 중심 플레이어 조회 화면을 만드는 것입니다.

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

추후 프론트엔드에서 사용할 서비스 전용 API:

```http
GET /api/players/{tekkenId}
GET /api/players/{tekkenId}/matches
GET /api/players/{tekkenId}/summary
GET /api/players/{tekkenId}/character-stats
```

## 6. 계획된 소스 구조

현재 소스는 `main.jsx`, `styles.css`만 있는 초기 상태입니다. 기능이 추가되면 아래 구조로 확장합니다.

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
    components
    hooks
    styles
    utils
```

## 7. MVP 프론트엔드 작업 순서

1. 현재 헬스체크 화면을 검색 중심 홈 화면으로 교체합니다.
2. Tekken ID 검색 폼을 추가합니다.
3. `shared/api` 아래에 백엔드 API 클라이언트를 추가합니다.
4. `PlayerPage` 라우트를 추가합니다.
5. `/api/players/{tekkenId}` 응답으로 플레이어 요약 정보를 렌더링합니다.
6. `/api/players/{tekkenId}/matches` 응답으로 최근 경기 목록을 렌더링합니다.
7. 캐릭터 사용률과 승률 패널을 추가합니다.

## 8. OS별 실행 명령

`npm` 명령은 macOS/Linux와 Windows PowerShell에서 동일하게 사용합니다.

```bash
npm install
npm run dev
npm run build
```
