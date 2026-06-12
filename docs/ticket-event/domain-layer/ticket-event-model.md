# TicketEventModel

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/model/TicketEventModel.kt`

티켓 이벤트의 루트 모델. 예매 일정·상태와 셋업 진행 단계를 담는다.

```kotlin
data class TicketEventModel(
    val id: Long? = null,
    val ticketEventName: String,
    val ticketOpenAt: Instant,        // 예매 오픈 시각
    val ticketClosedAt: Instant,      // 예매 마감 시각
    val ticketEventAt: Instant,       // 실제 이벤트(공연/경기) 시각
    val ticketEventStatus: TicketEventStatus = TicketEventStatus.SCHEDULED,
    val ticketCreationStatus: TicketCreationStatus = TicketCreationStatus.EVENT_CREATED,
    val ticketEventCategory: TicketEventCategory,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(ticketEventName.isNotBlank()) { "ticketEventName must not be blank" }
        require(ticketClosedAt.isAfter(ticketOpenAt)) { "ticketClosedAt must be after ticketOpenAt" }
        require(!ticketEventAt.isBefore(ticketClosedAt)) { "ticketEventAt must not be before ticketClosedAt" }
    }

    fun isBookable(at: Instant): Boolean = /* OPEN 이며 오픈~마감 구간 안 */
    fun isCreationCompleted(): Boolean = ticketCreationStatus == TicketCreationStatus.COMPLETED
}
```

- **불변 data class** — 모든 상태 변경은 `copy` 기반 메서드로 새 인스턴스를 반환한다.
- **생성 불변식(`init`)**: 이름 공백 금지, 마감은 오픈 이후, 이벤트 시각은 마감 이후(같거나 늦음).
- `isBookable(at)` — 주어진 시각이 `OPEN` 상태이며 오픈~마감 구간(마감 미포함) 안인지 판정한다.
  구체적으로 `ticketEventStatus == OPEN && at >= ticketOpenAt && at < ticketClosedAt`.
- `isCreationCompleted()` — 셋업이 `COMPLETED` 인지 여부. 예매 오픈 가능 조건의 하나.
- `id == null` 은 아직 영속화되지 않은 신규 모델을 의미한다.

## 두 가지 상태 축

이벤트는 **예매 진행 상태(`ticketEventStatus`)** 와 **셋업 진행 단계(`ticketCreationStatus`)** 라는
서로 독립적인 두 상태를 가진다. 전자는 예매 운영(오픈/마감/매진/취소), 후자는 등록 구성 단계를 추적한다.
각 enum 의 값 정의는 [enums.md](./enums.md) 참고.

### 예매 상태 전이 (`TicketEventStatus`)

내부 `transitionTo(target, allowedFrom)` 헬퍼가 허용 출발 상태를 검증하고,
위반 시 [`TicketEventException.InvalidStatusTransition`](./exceptions.md) 을 던진다.

| 메서드 | 허용 출발 상태 | 결과 |
|--------|----------------|------|
| `open()` | `SCHEDULED` | `OPEN` |
| `close()` | `OPEN`, `SOLD_OUT` | `CLOSED` |
| `markSoldOut()` | `OPEN` | `SOLD_OUT` |
| `reopen()` | `SOLD_OUT` | `OPEN` |
| `cancel()` | `SCHEDULED`/`OPEN`/`CLOSED`/`SOLD_OUT` | `CANCELLED` |

> `markSoldOut()`/`reopen()` 은 잔여 좌석이 0이 되거나 환불로 다시 풀리는 시점에 호출한다.
> 잔여석 카운트는 **좌석 상태(단일 진실 원천)에서 파생**하며, 이벤트 모델이 카운트를 보유하지 않는다.

> 별도 헬퍼로 `withStatus(newStatus)` 가 있으며, 전이 메서드가 검증 후 최종 상태 적용에 사용한다.

### 셋업 단계 전이 (`TicketCreationStatus`)

생성 워크플로우는 **순방향으로만** 진행된다. `advanceCreationTo` 헬퍼가 검증하며 위반 시
동일하게 [`InvalidStatusTransition`](./exceptions.md).

| 메서드 | 허용 출발 단계 | 결과 |
|--------|----------------|------|
| `markSectionsCreated()` | `EVENT_CREATED` | `SECTION_CREATED` |
| `markSeatsCreated()` | `SECTION_CREATED` | `SEAT_CREATED` |
| `completeCreation()` | `SEAT_CREATED` | `COMPLETED` |
