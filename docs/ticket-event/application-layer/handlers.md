# 티켓 이벤트 — 핸들러

> 상위: [애플리케이션 레이어 인덱스](./README.md)

`application/ticketevent/handler/`

유스케이스 구현체. 도메인별로 분리하되 명령 핸들러는 모두 `@Service @Transactional`,
조회 핸들러는 `@Transactional(readOnly = true)`. 의존 포트 정의는 [ports.md](./ports.md).

## `TicketEventCommandHandler` (command)

이벤트 자신의 명령을 모두 구현: `create` / `update` / `open` / `close` / `cancel` / `complete`.

| 구현 유스케이스 | 동작 |
|-----------------|------|
| `CreateTicketEventUseCase` | 커맨드로 `TicketEventModel` 생성 후 저장 |
| `UpdateTicketEventUseCase` | 기존 엔티티 로드 후 편집 필드만 `copy` 갱신 |
| `OpenTicketEventUseCase` | `transition { it.open() }` |
| `CloseTicketEventUseCase` | `transition { it.close() }` |
| `CancelTicketEventUseCase` | `transition { it.cancel() }` |
| `CompleteTicketEventCreationUseCase` | `transition { it.completeCreation() }` |

- **`update`** — 기존 엔티티를 로드(없으면 `TicketEventNotFound`)해 **편집 가능한 필드만** `copy` 로 갱신한다.
  상태·생성시각은 보존하며, 상태 전이는 `open`/`close`/`cancel` 로만 일어난다.
- **`transition` 헬퍼** — 상태/단계 전이의 공통 흐름. `findById`(없으면 `TicketEventNotFound`) → 도메인 전이 메서드 적용 → `save`.
  허용되지 않은 전이는 도메인 예외(409)로 거부된다.
- **의존 포트**: `TicketEventPersistencePort`.

## `TicketEventSectionCommandHandler` (command)

`CreateSectionsUseCase` 구현 — `createSections`.

- 이벤트 로드(없으면 `TicketEventNotFound`) 후 `markSectionsCreated()` 로 `EVENT_CREATED → SECTION_CREATED` 전이.
  도메인이 `EVENT_CREATED` 에서만 전이를 허용하므로 먼저 검증·전이한다.
- 커맨드의 `SectionSpec` 들을 `TicketEventSectionModel` 로 매핑해 일괄 저장(`saveAll`)하고, 전이된 이벤트를 같은 트랜잭션에서 저장.
- 결과는 `SectionCreationResult`(갱신된 이벤트 + 생성된 구역 목록).
- **의존 포트**: `TicketEventPersistencePort`, `TicketEventSectionPersistencePort`.

## `TicketEventSeatCommandHandler` (command)

`CreateSeatsUseCase` 구현 — `createSeats`.

- 이벤트 로드(없으면 `TicketEventNotFound`) 후 `markSeatsCreated()` 로 `SECTION_CREATED → SEAT_CREATED` 전이.
- 해당 이벤트의 구역들을 `findByTicketEventId` 로 조회하고, **구역이 없으면 `SectionNotFound`**.
- **구역별 `capacity` 만큼 좌석을 자동 생성** (`1..capacity`). 좌석의 `ticketEventId` 는 소속 구역의 값을 단일 출처로 복사하고,
  `rowLabel` 은 `""`, `seatNumber` 는 순번으로 채운다.
- 좌석 일괄 저장 후 전이된 이벤트를 같은 트랜잭션에서 저장. 결과는 `SeatCreationResult`(갱신된 이벤트 + 생성된 좌석 수 `saved.size`).
- **의존 포트**: `TicketEventPersistencePort`, `TicketEventSectionPersistencePort`, `TicketEventSeatPersistencePort`.

## `TicketEventQueryHandler` (query, `readOnly`)

`GetTicketEventUseCase` · `SearchTicketEventsUseCase` 구현.

| 구현 유스케이스 | 동작 |
|-----------------|------|
| `getById` | `findById`(없으면 `TicketEventNotFound`) → `TicketEventResult.from` |
| `search` | `search(category, status)` 결과를 `TicketEventResult` 로 매핑 |

- **의존 포트**: `TicketEventPersistencePort`.
