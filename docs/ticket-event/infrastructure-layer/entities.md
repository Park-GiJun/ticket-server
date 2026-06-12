# 티켓 이벤트 — 엔티티

> 상위: [인프라스트럭처 레이어 인덱스](./README.md)

`entity/` 아래 JPA `@Entity` 3종. 각 엔티티는 도메인 모델([`../domain-layer/README.md`](../domain-layer/README.md))과
**무관한 영속성 표현**이며, `toModel()` / `fromModel()` 로만 도메인과 오간다.
시각 컬럼은 모두 Hibernate `@CreationTimestamp`(`updatable = false`) / `@UpdateTimestamp` 로 자동 관리하고,
enum 은 모두 `EnumType.STRING` 으로 저장한다(가독성·재정렬 안전성).

## TicketEventEntity

테이블 `ticket_events`.

```kotlin
@Entity
@Table(name = "ticket_events")
class TicketEventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var ticketEventName: String,
    @Column(nullable = false) var ticketOpenAt: Instant,
    @Column(nullable = false) var ticketClosedAt: Instant,
    @Column(nullable = false) var ticketEventAt: Instant,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var ticketEventStatus: TicketEventStatus,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var ticketCreationStatus: TicketCreationStatus,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var ticketEventCategory: TicketEventCategory,
    @CreationTimestamp @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @UpdateTimestamp @Column(nullable = false) var updatedAt: Instant? = null,
)
```

| 컬럼 | 타입 | 비고 |
|------|------|------|
| `id` | `Long?` | IDENTITY 자동 증가 |
| `ticketEventName` | `String` | not null |
| `ticketOpenAt` / `ticketClosedAt` / `ticketEventAt` | `Instant` | 예매 시작·종료·공연 시각 |
| `ticketEventStatus` | `TicketEventStatus` | **예매 상태** 축 (enum string, length 20) |
| `ticketCreationStatus` | `TicketCreationStatus` | **셋업 단계** 축 (enum string, length 20) |
| `ticketEventCategory` | `TicketEventCategory` | enum string, length 20 |
| `createdAt` / `updatedAt` | `Instant?` | Hibernate 자동 |

- 예매 상태(`ticketEventStatus`)와 셋업 단계(`ticketCreationStatus`)를 **각각 별도 컬럼**으로 저장한다.
  두 상태 축은 서로 독립적이며 도메인에서 따로 전이된다.
- `fromModel` 은 시각/`id` 외 필드를 그대로 복사하고, `createdAt`/`updatedAt` 은 영속 시점에 Hibernate 가 채운다.

## TicketEventSectionEntity

테이블 `ticket_event_sections`. 이벤트 단위 구역 조회를 위해 `ticketEventId` 단일 인덱스를 둔다.

```kotlin
@Entity
@Table(
    name = "ticket_event_sections",
    indexes = [Index(name = "idx_section_event", columnList = "ticketEventId")],
)
class TicketEventSectionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var ticketEventId: Long,
    @Column(nullable = false) var sectionName: String,
    @Column(nullable = false, length = 30) var grade: String,
    @Column(nullable = false) var price: Long,
    @Column(nullable = false) var capacity: Int,
    @CreationTimestamp @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @UpdateTimestamp @Column(nullable = false) var updatedAt: Instant? = null,
)
```

| 컬럼 | 타입 | 비고 |
|------|------|------|
| `ticketEventId` | `Long` | 소속 이벤트 FK 값, `idx_section_event` 인덱스 |
| `sectionName` | `String` | not null |
| `grade` | `String` | length 30 (등급/가격 정책 단위) |
| `price` | `Long` | not null |
| `capacity` | `Int` | not null |

## TicketEventSeatEntity

테이블 `ticket_event_seats`. 수억 행까지 커질 수 있는 핫 테이블이므로 복합 인덱스로 받친다.

```kotlin
@Entity
@Table(
    name = "ticket_event_seats",
    indexes = [
        // 이벤트 단위 핫쿼리(잔여석 집계·매진 판정)를 단일 인덱스로 처리하기 위한 복합 인덱스.
        Index(name = "idx_seat_event_status", columnList = "ticketEventId, status"),
        Index(name = "idx_seat_section", columnList = "sectionId"),
    ],
)
class TicketEventSeatEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var sectionId: Long,
    @Column(nullable = false) var ticketEventId: Long,   // 비정규화(생성 후 불변)
    @Column(nullable = false, length = 10) var rowLabel: String,
    @Column(nullable = false) var seatNumber: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var status: SeatStatus,
    @CreationTimestamp @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @UpdateTimestamp @Column(nullable = false) var updatedAt: Instant? = null,
)
```

| 컬럼 | 타입 | 비고 |
|------|------|------|
| `sectionId` | `Long` | 소속 구역 FK 값, `idx_seat_section` 인덱스 |
| `ticketEventId` | `Long` | **비정규화** 값. 구역으로부터 도달 가능하지만 이벤트 단위 조회/파티셔닝을 위해 직접 보유. **생성 후 불변** |
| `rowLabel` | `String` | length 10 (열 라벨) |
| `seatNumber` | `Int` | not null |
| `status` | `SeatStatus` | enum string, length 20 |

### 인덱스 설계

- `idx_seat_event_status (ticketEventId, status)` — 이벤트 단위 잔여석 집계·매진 판정을 단일 복합 인덱스로 받친다.
  `WHERE ticketEventId = ? [GROUP BY status]` 형태의 핫쿼리가 인덱스만으로 처리된다.
- `idx_seat_section (sectionId)` — 구역 단위 좌석 조회.
- `ticketEventId` 비정규화 근거는 도메인 좌석 모델 문서([`../domain-layer/seat-model.md`](../domain-layer/seat-model.md)) 참고.
  좌석 테이블은 대규모로 커질 수 있어 향후 `ticketEventId` 기준 파티셔닝 여지를 남긴다.
