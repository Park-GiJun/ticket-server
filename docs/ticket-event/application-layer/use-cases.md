# 티켓 이벤트 — 인바운드 포트 (유스케이스)

> 상위: [애플리케이션 레이어 인덱스](./README.md)

`application/ticketevent/port/in/`

**1 인터페이스 = 1 함수** 규칙을 따르며, 도메인별 파일로 나눈다.
구역/좌석 생성은 이벤트 명령에서 분리해 각 도메인 유스케이스로 둔다.

> **인가(누가 호출할 수 있는가)는 유스케이스가 아니라 웹/보안 계층**에서 처리한다.
> 유스케이스 시그니처에 토큰/사용자를 넣지 않는다.

## 이벤트 명령 — `port/in/command/TicketEventCommandUseCases.kt`

이벤트 자신의 명령. 구현체는 [`TicketEventCommandHandler`](./handlers.md).

```kotlin
interface CreateTicketEventUseCase { fun create(command: CreateTicketEventCommand): TicketEventResult }
interface UpdateTicketEventUseCase { fun update(command: UpdateTicketEventCommand): TicketEventResult }
interface OpenTicketEventUseCase  { fun open(id: Long): TicketEventResult }
interface CloseTicketEventUseCase { fun close(id: Long): TicketEventResult }
interface CancelTicketEventUseCase { fun cancel(id: Long): TicketEventResult }
// 셋업 4단계: 생성 단계 전이(SEAT_CREATED → COMPLETED)
interface CompleteTicketEventCreationUseCase { fun complete(id: Long): TicketEventResult }
```

- `open`/`close`/`cancel` 은 **예매 상태(`TicketEventStatus`)** 전이, `complete` 는 **생성 단계(`TicketCreationStatus`)** 전이로 서로 다른 상태 축이다.
- `update` 는 식별자/편집 필드만 받는다(상태·생성시각은 보존). 자세한 동작은 [handlers.md](./handlers.md).

## 구역 명령 — `port/in/command/TicketEventSectionCommandUseCases.kt`

```kotlin
// 셋업 2단계: 이벤트를 EVENT_CREATED → SECTION_CREATED 로 진행
interface CreateSectionsUseCase { fun createSections(command: CreateSectionsCommand): SectionCreationResult }
```

## 좌석 명령 — `port/in/command/TicketEventSeatCommandUseCases.kt`

```kotlin
// 셋업 3단계: 이벤트를 SECTION_CREATED → SEAT_CREATED 로 진행
interface CreateSeatsUseCase { fun createSeats(command: CreateSeatsCommand): SeatCreationResult }
```

## 이벤트 조회 — `port/in/query/TicketEventQueryUseCases.kt`

구현체는 [`TicketEventQueryHandler`](./handlers.md).

```kotlin
interface GetTicketEventUseCase { fun getById(id: Long): TicketEventResult }
interface SearchTicketEventsUseCase { fun search(query: SearchTicketEventsQuery): List<TicketEventResult> }
```

## 구역 조회 — `port/in/query/TicketEventSectionQueryUseCases.kt`

구현체는 [`TicketEventSectionQueryHandler`](./handlers.md).

```kotlin
interface GetSectionUseCase { fun getById(ticketEventId: Long, id: Long): TicketEventSectionResult }
interface ListSectionsByEventUseCase { fun listByEvent(ticketEventId: Long): List<TicketEventSectionResult> }
```

- `getById` 는 `ticketEventId` 를 함께 받아 **소속 검증**한다. 구역이 그 이벤트 소속이 아니면 없음(`SectionNotFound`)으로 취급.

## 좌석 조회 — `port/in/query/TicketEventSeatQueryUseCases.kt`

구현체는 [`TicketEventSeatQueryHandler`](./handlers.md).

```kotlin
interface GetSeatUseCase { fun getById(ticketEventId: Long, id: Long): TicketEventSeatResult }
interface ListSeatsByEventUseCase { fun listByEvent(ticketEventId: Long): List<TicketEventSeatResult> }
interface GetSeatAvailabilityUseCase { fun getAvailability(ticketEventId: Long): SeatAvailabilityResult }
```

- `getById` 는 구역과 마찬가지로 `ticketEventId` 소속 검증을 포함한다(불일치 시 `SeatNotFound`).
- `getAvailability` 는 상태별 좌석 수 집계를 합계/잔여석과 함께 반환한다([dto.md](./dto.md)의 `SeatAvailabilityResult`).

## 셋업(생성) 워크플로우와 유스케이스 매핑

이벤트는 4단계로 셋업되며, 각 단계는 도메인 모델의 단계 전이([도메인 레이어](../domain-layer/README.md))를
강제한다. 순서를 어기면 `InvalidStatusTransition`(409).

| 단계 | 유스케이스 | 생성 단계 전이 |
|------|-----------|----------------|
| 1. 이벤트 생성 | `CreateTicketEventUseCase` | (초기) `EVENT_CREATED` |
| 2. 구역 생성 | `CreateSectionsUseCase` | `EVENT_CREATED → SECTION_CREATED` |
| 3. 좌석 생성 | `CreateSeatsUseCase` | `SECTION_CREATED → SEAT_CREATED` |
| 4. 완료 | `CompleteTicketEventCreationUseCase` | `SEAT_CREATED → COMPLETED` |

입출력 DTO 정의는 [dto.md](./dto.md) 참고.
