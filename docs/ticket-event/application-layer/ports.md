# 티켓 이벤트 — 아웃바운드 포트

> 상위: [애플리케이션 레이어 인덱스](./README.md)

`application/ticketevent/port/out/persistence/`

핸들러가 의존하는 영속화 추상. 이벤트/구역/좌석 3종으로 분리한다.
구현체는 인프라 레이어의 어댑터들에 둔다.

## `TicketEventPersistencePort`

```kotlin
interface TicketEventPersistencePort {
    fun save(ticketEvent: TicketEventModel): TicketEventModel
    fun findById(id: Long): TicketEventModel?
    fun existsById(id: Long): Boolean
    /** 카테고리/상태로 필터링해 조회한다(둘 다 null 이면 전체). */
    fun search(category: TicketEventCategory?, status: TicketEventStatus?): List<TicketEventModel>
}
```

## `TicketEventSectionPersistencePort`

```kotlin
interface TicketEventSectionPersistencePort {
    fun saveAll(sections: List<TicketEventSectionModel>): List<TicketEventSectionModel>
    fun findById(id: Long): TicketEventSectionModel?
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSectionModel>
}
```

- `saveAll` 은 구역 일괄 생성에, `findByTicketEventId` 는 좌석 생성 시 구역 조회에 쓰인다([handlers.md](./handlers.md)).

## `TicketEventSeatPersistencePort`

```kotlin
interface TicketEventSeatPersistencePort {
    fun saveAll(seats: List<TicketEventSeatModel>): List<TicketEventSeatModel>
    fun findById(id: Long): TicketEventSeatModel?
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSeatModel>
    // 매진 판정: 특정 상태 좌석 존재 여부를 COUNT 보다 싸게 확인
    fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean
    // 상태별 좌석 수 집계(잔여석/판매 수 폴백)
    fun countByTicketEventIdGroupedByStatus(ticketEventId: Long): Map<SeatStatus, Long>
}
```

- **`existsByTicketEventIdAndStatus`** — 이벤트 내 특정 상태의 좌석이 하나라도 존재하는지. **매진(SOLD_OUT) 판정**에 사용한다.
  마지막 판매 후 `AVAILABLE` 좌석이 남았는지를 `COUNT(*)` 보다 싸게(존재 여부만) 확인한다.
- **`countByTicketEventIdGroupedByStatus`** — 이벤트 내 **상태별 좌석 수 집계**(잔여석/판매 수 표시용 폴백).
  진실 원천은 좌석 상태이며, 고빈도 조회는 추후 Redis 프로젝션/읽기 모델로 분리할 수 있다.

도메인 모델 정의는 [도메인 레이어](../domain-layer/README.md), 구현 어댑터는 인프라 레이어 문서를 참고한다.
