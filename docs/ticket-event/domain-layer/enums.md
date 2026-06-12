# enum 들

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/enums/` — User 의 공유 enum 과 동일한 위치 규칙.

## TicketEventStatus

`domain/enums/TicketEventStatus.kt` — 티켓 이벤트의 **예매 진행 상태**.

```kotlin
enum class TicketEventStatus {
    SCHEDULED, OPEN, CLOSED, SOLD_OUT, CANCELLED, COMPLETED,
}
```

| 값 | 의미 |
|----|------|
| `SCHEDULED` | 예매 오픈 전(등록만 된 상태) |
| `OPEN` | 예매 진행 중 |
| `CLOSED` | 예매 마감(오픈 기간 종료) |
| `SOLD_OUT` | 전 좌석 매진 |
| `CANCELLED` | 이벤트 취소 |
| `COMPLETED` | 이벤트 종료(공연/경기 완료) |

> 상태 전이표는 [ticket-event-model.md](./ticket-event-model.md#예매-상태-전이-ticketeventstatus) 참고.

## TicketCreationStatus

`domain/enums/TicketCreationStatus.kt` — 이벤트의 **셋업(생성) 진행 단계**.
예매 진행 상태와는 별개로 등록이 어디까지 구성됐는지를 추적하며, 단계는 순방향으로만 진행된다.

```kotlin
enum class TicketCreationStatus {
    EVENT_CREATED, SECTION_CREATED, SEAT_CREATED, COMPLETED,
}
```

| 순서 | 값 | 의미 |
|------|----|------|
| 1 | `EVENT_CREATED` | 이벤트 기본 정보만 생성된 상태 |
| 2 | `SECTION_CREATED` | 좌석 구역 구성 완료 |
| 3 | `SEAT_CREATED` | 개별 좌석 생성 완료 |
| 4 | `COMPLETED` | 셋업 완료 — 예매 오픈 가능 |

> 단계 전이표는 [ticket-event-model.md](./ticket-event-model.md#셋업-단계-전이-ticketcreationstatus) 참고.

## SeatStatus

`domain/enums/SeatStatus.kt` — **개별 좌석 상태**.

```kotlin
enum class SeatStatus {
    AVAILABLE, HELD, SOLD, BLOCKED,
}
```

| 값 | 의미 |
|----|------|
| `AVAILABLE` | 예매 가능 |
| `HELD` | 결제 진행을 위한 임시 점유(홀드) |
| `SOLD` | 판매 완료 |
| `BLOCKED` | 판매 대상에서 제외(시야 제한석·관계자석 등) |

> 좌석 상태 전이표는 [seat-model.md](./seat-model.md#좌석-상태-전이-seatstatus) 참고.

## TicketEventCategory

`domain/enums/TicketEventCategory.kt` — 티켓 이벤트의 분류.

```kotlin
enum class TicketEventCategory {
    CONCERT, MUSICAL, PLAY, SPORTS, EXHIBITION, FESTIVAL, ETC,
}
```

| 값 | 의미 |
|----|------|
| `CONCERT` | 콘서트 |
| `MUSICAL` | 뮤지컬 |
| `PLAY` | 연극 |
| `SPORTS` | 스포츠 경기 |
| `EXHIBITION` | 전시 |
| `FESTIVAL` | 축제 |
| `ETC` | 기타 |
