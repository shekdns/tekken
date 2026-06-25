# Asset Policy

## 목적

T8LAB은 철권8 전적 사이트이므로 캐릭터 초상화, 아이콘, 배경 이미지가 서비스 완성도에 큰 영향을 줍니다. 다만 Tekken 8 캐릭터와 공식 이미지는 Bandai Namco Entertainment의 지식재산권에 속할 수 있으므로, 자산은 출처와 사용 권한을 확인한 뒤 단계적으로 반영합니다.

이 문서는 캐릭터 이미지 자산을 추가하기 전 확인해야 할 기준과 저장 규칙을 정리합니다.

## 기본 원칙

1. 출처가 불명확한 이미지는 저장소에 추가하지 않습니다.
2. 공식 이미지, 팬 위키 이미지, 커뮤니티 이미지, 직접 제작 이미지는 서로 다른 승인 상태로 관리합니다.
3. 실제 서비스 배포에 사용할 이미지는 라이선스, 사용 조건, 출처 URL, 확인 날짜를 함께 기록합니다.
4. 권리 확인이 끝나기 전에는 현재처럼 텍스트 fallback 또는 추상 실루엣 fallback을 사용합니다.
5. 이미지 파일은 커밋 전에 크기, 포맷, 파일명, 출처 메타데이터를 검수합니다.

## 자산 소스 우선순위

### 1. 직접 제작 자산

우선순위가 가장 높습니다.

- T8LAB 전용 실루엣, 색상 패턴, 이니셜 배지, 추상 캐릭터 카드.
- 특정 Tekken 캐릭터의 외형을 그대로 복제하지 않는 방향.
- 저작권 리스크가 가장 낮고 디자인 톤을 독립적으로 만들기 쉽습니다.

초기 권장안:

- MVP와 초기 디자인 고도화 단계에서는 직접 제작 fallback을 기본으로 둡니다.
- 캐릭터별 고유 색상, 이름, 이니셜, 간단한 프레임을 조합합니다.

### 2. 공식 press/media kit 자산

사용 조건을 확인할 수 있을 때만 사용합니다.

- Bandai Namco 또는 Tekken 공식 채널의 press kit, media kit, 공식 스크린샷.
- 사용 범위가 명시되어 있지 않으면 저장소에 포함하지 않습니다.
- 사용 가능하더라도 출처와 사용 조건을 `asset-manifest`에 기록합니다.

검토 기준:

- 비상업/팬사이트 사용 가능 여부.
- 이미지 재가공 가능 여부.
- 로고, 워터마크, 저작권 고지 필요 여부.
- 서비스 배포 환경에서 사용 가능한지 여부.

### 3. 제3자 위키/커뮤니티 이미지

기본적으로 보류합니다.

- 팬 위키, 커뮤니티, SNS, 이미지 검색 결과는 원 저작권자와 업로더 권리가 섞여 있을 수 있습니다.
- 라이선스가 명확해도 Tekken 캐릭터 이미지 자체의 권리 문제가 남을 수 있습니다.
- 프로덕션 자산으로 쓰기 전에 별도 검토가 필요합니다.

### 4. AI 생성 이미지

Tekken 캐릭터를 닮게 생성하는 방식은 사용하지 않습니다.

- 특정 캐릭터 외형, 복장, 얼굴, 포즈를 재현하는 이미지는 피합니다.
- 사용할 경우 T8LAB 고유의 추상 배경, 패턴, UI 장식 등으로 제한합니다.
- 캐릭터 초상화 대체 용도로는 권장하지 않습니다.

## 파일 저장 규칙

프론트엔드는 아래 public 경로를 사용합니다.

```text
frontend/public/assets/characters/{assetKey}.webp
```

예시:

```text
frontend/public/assets/characters/dragunov.webp
frontend/public/assets/characters/kazuya.webp
frontend/public/assets/characters/reina.webp
```

`assetKey`는 backend `GET /api/characters/options` 응답의 `assetKey`와 동일해야 합니다.

## 이미지 스펙

초기 권장 스펙:

- 포맷: `webp`
- 기본 크기: 512 x 512 이상
- 화면 사용 비율: 정사각형 또는 상반신 중심 portrait
- 배경: 투명 또는 어두운 UI에서도 보이는 단순 배경
- 파일명: 소문자 kebab-case 또는 backend `assetKey`
- 최대 용량: 파일당 200KB 권장, 500KB 초과 시 최적화 필요

## Manifest 규칙

실제 이미지를 추가할 때는 출처 추적용 manifest를 함께 둡니다.

권장 경로:

```text
frontend/public/assets/characters/asset-manifest.json
```

권장 필드:

```json
[
  {
    "assetKey": "dragunov",
    "file": "/assets/characters/dragunov.webp",
    "sourceType": "official_press_kit",
    "sourceUrl": "https://example.com/source",
    "license": "확인된 사용 조건",
    "attribution": "필요한 저작권 표기",
    "checkedAt": "2026-06-25",
    "approvedForProduction": false,
    "notes": "검토 메모"
  }
]
```

`approvedForProduction`이 `false`인 이미지는 로컬 검토용으로만 취급합니다.

## 검수 체크리스트

이미지를 추가하기 전 아래 항목을 확인합니다.

- 출처 URL이 남아 있는가?
- 사용 조건을 확인했는가?
- 상업/비상업 서비스 배포 가능성이 확인됐는가?
- 파일명이 backend `assetKey`와 일치하는가?
- 이미지 크기와 용량이 기준에 맞는가?
- 어두운 배경, 밝은 배경에서 모두 식별 가능한가?
- 모바일 목록 UI에서 너무 복잡하게 보이지 않는가?
- fallback 없이 깨진 이미지가 표시되지 않는가?

## 현재 결정

초기 프로덕션 기준은 다음과 같습니다.

1. 공식/제3자 캐릭터 이미지는 권리 확인 전까지 저장소에 추가하지 않습니다.
2. 프론트엔드는 현재 텍스트 fallback을 유지합니다.
3. 디자인 고도화 단계에서는 T8LAB 고유 실루엣 또는 색상 기반 placeholder를 먼저 개선합니다.
4. 공식 press kit 사용 가능 여부가 확인되면 `asset-manifest.json`과 함께 단계적으로 추가합니다.

## 참고 링크

- Tekken 8은 Bandai Namco Studios/Arika가 개발하고 Bandai Namco Entertainment가 배급한 게임입니다.
  - https://en.wikipedia.org/wiki/Tekken_8
- 일본 IP 보유사들이 무단 IP 사용에 민감하게 대응하고 있다는 최근 사례가 있으므로, 캐릭터 유사 이미지와 AI 생성 이미지는 보수적으로 다룹니다.
  - https://www.theverge.com/news/812545/coda-studio-ghibli-sora-2-copyright-infringement
