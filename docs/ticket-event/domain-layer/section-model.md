# TicketEventSectionModel

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/model/TicketEventSectionModel.kt`

하나의 이벤트에 속하는 좌석 **구역**. 등급/가격 정책의 단위다. 개별 좌석
([TicketEventSeatModel](./seat-model.md))은 구역에 소속되며, 좌석의 가격·등급은 소속 구역으로부터 결정된다.

```kotlin
data class TicketEventSectionModel(
    val id: Long? = null,
    val ticketEventId: Long,   // 소속 이벤트
    val sectionName: String,   // 예: "VIP석", "1층 A구역", "스탠딩"
    val grade: String,         // 예: "VIP", "R", "S" — 이벤트마다 체계가 달라 자유 문자열
    val price: Long,           // 좌석 1매 가격(원), 0 이상
    val capacity: Int,         // 구역 전체 좌석 수, 1 이상
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
```

- `ticketEventId` — 소속 [TicketEventModel](./ticket-event-model.md) 식별자.
- `grade` — 이벤트마다 등급 체계가 달라 자유 문자열로 둔다.
- **생성 불변식(`init`)**:
  - `sectionName` 공백 금지
  - `grade` 공백 금지
  - `price >= 0`
  - `capacity >= 1`
- 별도의 상태 전이는 없다(불변 data class).
