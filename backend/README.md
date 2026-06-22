# T8LAB Backend

T8LAB의 백엔드 프로젝트입니다. Spring Boot 기반으로 철권8 플레이어 조회, EWGF API 프록시, PostgreSQL 기반 캐싱, 향후 통계 기능을 담당합니다.

현재 백엔드는 EWGF 프록시 API를 제공합니다. 다음 목표는 PostgreSQL을 사용하는 서비스 전용 `player`, `match`, `stats`, `cache` API를 추가하는 것입니다.

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
```

macOS/Linux:

```bash
export EWGF_API_KEY=replace_with_your_ewgf_api_key
export DB_URL=jdbc:postgresql://localhost:5432/tekken
export DB_USERNAME=tekken
export DB_PASSWORD=tekken
```

Windows PowerShell:

```powershell
$env:EWGF_API_KEY="replace_with_your_ewgf_api_key"
$env:DB_URL="jdbc:postgresql://localhost:5432/tekken"
$env:DB_USERNAME="tekken"
$env:DB_PASSWORD="tekken"
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
```

`POST /api/ewgf/profile` 요청 본문:

```json
["27tB-4yhF-mfNE"]
```

## 7. 예정된 서비스 전용 API

장기적으로 EWGF 응답 구조를 프론트엔드에 직접 노출하지 않습니다. 백엔드에서 외부 데이터를 T8LAB 전용 DTO로 변환합니다.

예정 API:

```http
GET /api/players/{tekkenId}
GET /api/players/{tekkenId}/matches
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

현재 코드는 초기 구현인 `external` 패키지를 사용합니다. 이후 아래 구조로 점진적으로 이동합니다.

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
  match
  stats
  leaderboard
  cache
```

## 10. 백엔드 구현 순서

1. 기존 EWGF 프록시 API를 안정적으로 유지합니다.
2. 초기 테이블에 대응하는 JPA Entity와 Repository를 추가합니다.
3. `datasource.ewgf` 패키지를 만들고 EWGF 클라이언트 코드를 이동합니다.
4. EWGF Profile API와 DB 캐시를 사용하는 `player` API를 추가합니다.
5. EWGF Battles API와 DB 캐시를 사용하는 `match` API를 추가합니다.
6. 기본 승률, 캐릭터별 사용률, 캐릭터별 승률을 계산하는 `stats` 서비스를 추가합니다.
7. 프론트엔드 호출을 `/api/ewgf/*`에서 `/api/players/*`로 전환합니다.
