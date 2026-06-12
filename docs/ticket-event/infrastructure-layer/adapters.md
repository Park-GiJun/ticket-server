# 티켓 이벤트 — 어댑터

> 상위: [인프라스트럭처 레이어 인덱스](./README.md)

`adapter/` 아래 `@Component` 3종. 각 어댑터는 대응 아웃바운드 포트
([`../application-layer/ports.md`](../application-layer/ports.md))를 구현하며,
리포지토리 호출과 **엔티티↔도메인 변환**을 담당한다.

## TicketEventPersistenceAdapter

`TicketEventPersistencePort` 구현.

```kotlin
override fun save(ticketEvent: TicketEventModel): TicketEventModel {
    val entity = ticketEvent.id
        ?.let { id -> repository.findById(id).orElse(null)?.applyFrom(ticketEvent) }
        ?: TicketEventEntity.fromModel(ticketEvent)
    return repository.save(entity).toModel()
}

/** 기존 영속 엔티티에 도메인 변경분을 반영한다(상태/일정 등 갱신용). */
private fun TicketEventEntity.applyFrom(model: TicketEventModel): TicketEventEntity = apply {
    ticketEventName = model.ticketEventName
    ticketOpenAt = model.ticketOpenAt
    ticketClosedAt = model.ticketClosedAt
    ticketEventAt = model.ticketEventAt
    ticketEventStatus = model.ticketEventStatus
    ticketCreationStatus = model.ticketCreationStatus
    ticketEventCategory = model.ticketEventCategory
}
```

- `save` 는 **id 가 있으면** 기존 엔티티를 로드해 변경분을 반영하고(JPA **dirty checking**),
  **없으면** `fromModel` 로 신규 생성한다. (id 가 있으나 행이 없으면 `fromModel` 폴백.)
- `applyFrom` 은 이름/일정/**예매상태(`ticketEventStatus`)**/**셋업단계(`ticketCreationStatus`)**/카테고리를 갱신한다.
  `createdAt`/`id` 는 기존 엔티티의 값을 그대로 두어 **생성 시각이 보존**된다(`updatedAt` 은 Hibernate 가 갱신).
- 그 외: `findById` / `existsById` / `search`(리포지토리 위임 후 `toModel` 매핑).

## TicketEventSectionPersistenceAdapter

`TicketEventSectionPersistencePort` 구현.

```kotlin
override fun saveAll(sections: List<TicketEventSectionModel>): List<TicketEventSectionModel> =
    repository.saveAll(sections.map { TicketEventSectionEntity.fromModel(it) }).map { it.toModel() }
```

- `saveAll` — 구역 목록을 일괄 신규 저장(이벤트 셋업 시 벌크 생성).
- `findById` / `findByTicketEventId` — 리포지토리 위임 후 `toModel` 매핑.

## TicketEventSeatPersistenceAdapter

`TicketEventSeatPersistencePort` 구현.

```kotlin
override fun saveAll(seats: List<TicketEventSeatModel>): List<TicketEventSeatModel> =
    repository.saveAll(seats.map { TicketEventSeatEntity.fromModel(it) }).map { it.toModel() }

override fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean =
    repository.existsByTicketEventIdAndStatus(ticketEventId, status)

override fun countByTicketEventIdGroupedByStatus(ticketEventId: Long): Map<SeatStatus, Long> =
    repository.countGroupedByStatus(ticketEventId).associate { it.status to it.cnt }
```

- `saveAll` — 좌석 일괄 신규 저장(대량).
- `findById` / `findByTicketEventId` — 리포지토리 위임 후 `toModel` 매핑.
- `existsByTicketEventIdAndStatus` — 매진 판정 위임(존재 여부 확인).
- `countByTicketEventIdGroupedByStatus` — 리포지토리의 `countGroupedByStatus` 프로젝션 리스트를
  `associate { it.status to it.cnt }` 로 **`Map<SeatStatus, Long>`** 으로 변환해 반환한다.
  포트명(`countByTicketEventIdGroupedByStatus`)과 리포지토리 쿼리명(`countGroupedByStatus`)이 다른 점에 유의.
