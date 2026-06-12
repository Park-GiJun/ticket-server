# 티켓 이벤트 — 리포지토리

> 상위: [인프라스트럭처 레이어 인덱스](./README.md)

`repository/` 아래 Spring Data `JpaRepository` 3종. 기본 CRUD 는 상속으로 얻고,
필요한 조회만 파생 쿼리·`@Query` 로 추가한다.

## TicketEventPersistenceRepository

```kotlin
interface TicketEventPersistenceRepository : JpaRepository<TicketEventEntity, Long> {

    @Query(
        """
        SELECT t FROM TicketEventEntity t
        WHERE (:category IS NULL OR t.ticketEventCategory = :category)
          AND (:status IS NULL OR t.ticketEventStatus = :status)
        """,
    )
    fun search(
        @Param("category") category: TicketEventCategory?,
        @Param("status") status: TicketEventStatus?,
    ): List<TicketEventEntity>
}
```

- `search` 는 **null-safe JPQL** 이다. `:category IS NULL OR ...` 패턴으로 두 필터를 선택적으로 적용한다.
  파라미터가 `null` 이면 해당 조건은 무시되어, 카테고리/상태 조합을 단일 쿼리로 처리한다.
- `@Param` 으로 명명 바인딩한다.

## TicketEventSectionPersistenceRepository

```kotlin
interface TicketEventSectionPersistenceRepository : JpaRepository<TicketEventSectionEntity, Long> {
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSectionEntity>
}
```

- `findByTicketEventId` — 파생 쿼리. `idx_section_event` 인덱스를 탄다.

## TicketEventSeatPersistenceRepository

```kotlin
interface TicketEventSeatPersistenceRepository : JpaRepository<TicketEventSeatEntity, Long> {
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSeatEntity>

    fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean

    @Query(
        """
        SELECT s.status AS status, COUNT(s) AS cnt FROM TicketEventSeatEntity s
        WHERE s.ticketEventId = :ticketEventId
        GROUP BY s.status
        """,
    )
    fun countGroupedByStatus(@Param("ticketEventId") ticketEventId: Long): List<SeatStatusCount>
}

/** countGroupedByStatus 의 프로젝션. */
interface SeatStatusCount {
    val status: SeatStatus
    val cnt: Long
}
```

- `findByTicketEventId` — 파생 쿼리. `idx_seat_event_status` 의 선두 컬럼을 탄다.
- `existsByTicketEventIdAndStatus` — 특정 상태 좌석의 **존재 여부**만 확인하는 경량 쿼리.
- `countGroupedByStatus` — 상태별 좌석 수 집계. **인터페이스 기반 프로젝션** `SeatStatusCount`(`status` / `cnt`)
  으로 결과를 받는다. JPQL 의 `AS status` / `AS cnt` 별칭이 프로젝션 프로퍼티명과 일치해야 한다.

> 매진 판정은 전체 `COUNT` 대신 `existsByTicketEventIdAndStatus(eventId, AVAILABLE)` 로 싸게 확인하고,
> 잔여석/판매 수 표시는 `countGroupedByStatus` 집계로 폴백한다. 고빈도 조회는 추후 Redis 읽기 모델로 분리할 수 있다.
> 두 쿼리 모두 `idx_seat_event_status` 복합 인덱스를 활용한다. 포트 측 의도는
> [`../application-layer/ports.md`](../application-layer/ports.md) 참고.
