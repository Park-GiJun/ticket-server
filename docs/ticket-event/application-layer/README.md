# 티켓 이벤트 — 애플리케이션 레이어

> 상위: [티켓 이벤트 도메인 인덱스](../README.md)

User 와 동일한 CQRS 구조(포트 in/out · DTO · 핸들러)를 갖춘다.
DTO 는 `ticketevent` 아래에 통합하되, **유스케이스와 핸들러는 도메인(이벤트/구역/좌석)별로 분리**한다.

## CQRS 구조 개요

- **인바운드 포트(유스케이스)** 는 외부(웹 어댑터)가 호출하는 진입점이다. **1 인터페이스 = 1 함수** 규칙을 따르고,
  명령(Command)과 조회(Query)를 패키지로 분리한다.
- **DTO** 는 유스케이스 경계를 넘는 입출력 계약(Command/Query/Result)이다. 도메인 모델을 그대로 노출하지 않는다.
- **핸들러** 는 유스케이스 구현체로, 도메인 모델을 조립·전이시키고 아웃바운드 포트로 영속화한다. 모두 `@Service @Transactional`.
- **아웃바운드 포트(영속화)** 는 핸들러가 의존하는 저장소 추상이다. 구현체는 인프라 레이어 어댑터에 둔다.

> **인가(누가 호출할 수 있는가)는 유스케이스가 아니라 웹/보안 계층**에서 처리한다.
> 유스케이스 시그니처에 토큰/사용자를 넣지 않는다.

## 패키지 구조

`application/ticketevent/` 아래 관심사별 하위 패키지로 정리한다.

```
dto/
  command/   TicketEventCommands.kt   (이벤트·구역·좌석 커맨드 통합)
  query/     TicketEventQueries.kt
  result/    TicketEventResults.kt    (이벤트·구역·셋업 결과 통합)
port/
  in/
    command/ TicketEventCommandUseCases.kt · TicketEventSectionCommandUseCases.kt · TicketEventSeatCommandUseCases.kt
    query/   TicketEventQueryUseCases.kt
  out/
    persistence/ TicketEventPersistencePort.kt · TicketEventSectionPersistencePort.kt · TicketEventSeatPersistencePort.kt
handler/
  command/   TicketEventCommandHandler.kt · TicketEventSectionCommandHandler.kt · TicketEventSeatCommandHandler.kt
  query/     TicketEventQueryHandler.kt
```

## 세부 문서

| 문서 | 내용 |
|------|------|
| [use-cases.md](./use-cases.md) | 인바운드 포트(유스케이스) — 도메인별 인터페이스와 "1 인터페이스 = 1 함수" 규칙, 셋업 4단계 매핑 |
| [dto.md](./dto.md) | Command / Query / Result DTO 목록과 각 필드, `SectionCreationResult` / `SeatCreationResult` 구성 |
| [handlers.md](./handlers.md) | 4개 핸들러의 책임과 의존 포트, `update` 의 편집 필드 갱신·`transition` 헬퍼·좌석 자동 생성 로직 |
| [ports.md](./ports.md) | 아웃바운드 포트 3종 시그니처와 매진 판정·상태별 집계 메서드 설명 |

## 셋업(생성) 워크플로우 요약

이벤트는 **4단계**로 셋업된다. 각 단계는 도메인 모델의 단계 전이([도메인 레이어](../domain-layer/README.md))를
강제하므로, 순서를 어기면 `InvalidStatusTransition`(409) 이 된다.

| 단계 | 유스케이스 | 핸들러 | 생성 단계 전이 |
|------|-----------|--------|----------------|
| 1. 이벤트 생성 | `CreateTicketEventUseCase` | `TicketEventCommandHandler` | (초기) `EVENT_CREATED` |
| 2. 구역 생성 | `CreateSectionsUseCase` | `TicketEventSectionCommandHandler` | `EVENT_CREATED → SECTION_CREATED` |
| 3. 좌석 생성 | `CreateSeatsUseCase` | `TicketEventSeatCommandHandler` | `SECTION_CREATED → SEAT_CREATED` |
| 4. 완료 | `CompleteTicketEventCreationUseCase` | `TicketEventCommandHandler` | `SEAT_CREATED → COMPLETED` |

## 관련 레이어

- 도메인 모델/전이: [도메인 레이어](../domain-layer/README.md)
- 아웃바운드 포트 구현(어댑터) 및 REST 엔드포인트는 인프라 레이어 문서를 참고한다.
