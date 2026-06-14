# 티켓 이벤트 — DTO

> 상위: [애플리케이션 레이어 인덱스](./README.md)

`application/ticketevent/dto/`

유스케이스 경계를 넘는 입출력 계약. 도메인 모델을 그대로 노출하지 않고 `Result` 로 변환한다.

| 파일 | 타입 |
|------|------|
| `command/TicketEventCommands.kt` | `CreateTicketEventCommand`, `UpdateTicketEventCommand`, `CreateSectionsCommand`(+`SectionSpec`), `CreateSeatsCommand` |
| `query/TicketEventQueries.kt` | `SearchTicketEventsQuery` |
| `result/TicketEventResults.kt` | `TicketEventResult`, `TicketEventSectionResult`, `SectionCreationResult`, `SeatCreationResult`, `TicketEventSeatResult`, `SeatAvailabilityResult` |

## Command DTO

### `CreateTicketEventCommand`

| 필드 | 타입 | 비고 |
|------|------|------|
| `ticketEventName` | `String` | |
| `ticketOpenAt` | `Instant` | 예매 오픈 시각 |
| `ticketClosedAt` | `Instant` | 예매 마감 시각 |
| `ticketEventAt` | `Instant` | 공연/경기 일시 |
| `ticketEventCategory` | `TicketEventCategory` | |

상태/생성단계 필드는 받지 않는다 — 생성 시 도메인이 `EVENT_CREATED` 등 초기값을 설정한다.

### `UpdateTicketEventCommand`

`CreateTicketEventCommand` 와 동일 필드에 식별자 `id: Long` 을 더한 형태.

| 필드 | 타입 | 비고 |
|------|------|------|
| `id` | `Long` | 갱신 대상 식별자 |
| `ticketEventName` | `String` | 편집 가능 필드 |
| `ticketOpenAt` | `Instant` | 편집 가능 필드 |
| `ticketClosedAt` | `Instant` | 편집 가능 필드 |
| `ticketEventAt` | `Instant` | 편집 가능 필드 |
| `ticketEventCategory` | `TicketEventCategory` | 편집 가능 필드 |

상태·생성시각은 커맨드에 없으며 핸들러에서 보존한다([handlers.md](./handlers.md)).

### `CreateSectionsCommand` (+ 중첩 `SectionSpec`)

구역 일괄 생성(셋업 2단계: `EVENT_CREATED → SECTION_CREATED`).

| 필드 | 타입 | 비고 |
|------|------|------|
| `ticketEventId` | `Long` | 소속 이벤트 |
| `sections` | `List<SectionSpec>` | 생성할 구역 명세 |

`SectionSpec` (중첩 data class):

| 필드 | 타입 |
|------|------|
| `sectionName` | `String` |
| `grade` | `String` |
| `price` | `Long` |
| `capacity` | `Int` |

### `CreateSeatsCommand`

좌석 일괄 생성(셋업 3단계: `SECTION_CREATED → SEAT_CREATED`).

| 필드 | 타입 | 비고 |
|------|------|------|
| `ticketEventId` | `Long` | 소속 이벤트 |

좌석 명세는 받지 않는다 — 구역별 `capacity` 만큼 핸들러가 자동 생성하며, 좌석의 `ticketEventId` 는 소속 구역의 값을 복사한다([handlers.md](./handlers.md)).

## Query DTO

### `SearchTicketEventsQuery`

| 필드 | 타입 | 기본값 | 비고 |
|------|------|--------|------|
| `category` | `TicketEventCategory?` | `null` | 카테고리 필터 |
| `status` | `TicketEventStatus?` | `null` | 상태 필터 |

둘 다 `null` 이면 전체 조회.

## Result DTO

### `TicketEventResult`

도메인 `TicketEventModel` → DTO 변환(`from(model)` 팩토리). `id` 는 비-null 로 강제(`requireNotNull`).

| 필드 | 타입 |
|------|------|
| `id` | `Long` |
| `ticketEventName` | `String` |
| `ticketOpenAt` | `Instant` |
| `ticketClosedAt` | `Instant` |
| `ticketEventAt` | `Instant` |
| `ticketEventStatus` | `TicketEventStatus` |
| `ticketCreationStatus` | `TicketCreationStatus` |
| `ticketEventCategory` | `TicketEventCategory` |
| `createdAt` | `Instant?` |
| `updatedAt` | `Instant?` |

### `TicketEventSectionResult`

구역 1건. `from(model)` 팩토리(`id` 비-null 강제).

| 필드 | 타입 |
|------|------|
| `id` | `Long` |
| `ticketEventId` | `Long` |
| `sectionName` | `String` |
| `grade` | `String` |
| `price` | `Long` |
| `capacity` | `Int` |

### `SectionCreationResult`

구역 생성(셋업 2단계) 결과 = 갱신된 이벤트 + 생성된 구역 목록.

| 필드 | 타입 |
|------|------|
| `ticketEvent` | `TicketEventResult` |
| `sections` | `List<TicketEventSectionResult>` |

### `SeatCreationResult`

좌석 생성(셋업 3단계) 결과 = 갱신된 이벤트 + 생성된 좌석 수.

| 필드 | 타입 |
|------|------|
| `ticketEvent` | `TicketEventResult` |
| `createdSeatCount` | `Int` |

### `TicketEventSeatResult`

좌석 1건 조회 결과. `from(model)` 팩토리(`id` 비-null 강제).

| 필드 | 타입 |
|------|------|
| `id` | `Long` |
| `sectionId` | `Long` |
| `ticketEventId` | `Long` |
| `rowLabel` | `String` |
| `seatNumber` | `Int` |
| `status` | `SeatStatus` |

### `SeatAvailabilityResult`

이벤트의 좌석 잔여 현황. `from(ticketEventId, counts)` 팩토리가 누락 상태를 0 으로 채우고 합계/잔여석을 도출한다.

| 필드 | 타입 | 비고 |
|------|------|------|
| `ticketEventId` | `Long` | |
| `counts` | `Map<SeatStatus, Long>` | 4개 상태를 항상 포함(누락분 0) |
| `total` | `Long` | 상태별 좌석 수의 합 |
| `available` | `Long` | `AVAILABLE` 좌석 수 |
