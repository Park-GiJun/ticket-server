# TicketEventSeatModel

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/model/TicketEventSeatModel.kt`

구역에 속하는 **개별 좌석**. 예매 흐름의 최소 단위이며,
`AVAILABLE → HELD → SOLD` 의 상태 전이를 가진다.

```kotlin
data class TicketEventSeatModel(
    val id: Long? = null,
    val sectionId: Long,       // 소속 구역
    val ticketEventId: Long,   // 소속 이벤트(비정규화) — 아래 설명
    val rowLabel: String,      // 행 표기. 스탠딩 구역은 비어 있을 수 있음
    val seatNumber: Int,       // 행 내 좌석 번호, 1 이상
    val status: SeatStatus = SeatStatus.AVAILABLE,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(seatNumber >= 1) { "seatNumber must be at least 1" }
    }

    fun isAvailable(): Boolean = status == SeatStatus.AVAILABLE
}
```

- `sectionId` — 소속 [구역](./section-model.md) 식별자.
- `rowLabel` — 행 표기(예: "A", "12"). 비좌석형(스탠딩) 구역에서는 비어 있을 수 있다.
- **생성 불변식(`init`)**: `seatNumber >= 1`.
- `isAvailable()` — 좌석이 `AVAILABLE` 상태인지 여부.

## `ticketEventId` 비정규화 근거

좌석은 `sectionId → section.ticketEventId` 로 이벤트에 도달 가능하지만,
**이벤트 단위 핫쿼리(잔여석 집계·매진 판정)와 파티셔닝**을 위해 이벤트 ID를 의도적으로 중복 보유한다.
좌석↔이벤트는 생성 후 **불변**이라 갱신 이상(update anomaly)이 없으며,
생성 시 소속 구역의 값을 단일 출처로 복사한다.

## 좌석 상태 전이 (`SeatStatus`)

`transitionTo` 헬퍼가 허용 출발 상태를 검증하며, 위반 시
[`TicketEventException.InvalidSeatStatusTransition`](./exceptions.md) 을 던진다.
`SeatStatus` 값 정의는 [enums.md](./enums.md) 참고.

| 메서드 | 허용 출발 | 결과 | 용도 |
|--------|-----------|------|------|
| `hold()` | `AVAILABLE` | `HELD` | 결제 진행용 임시 점유 |
| `release()` | `HELD` | `AVAILABLE` | 점유 해제 |
| `sell()` | `HELD` | `SOLD` | 판매 확정 |
| `cancel()` | `SOLD` | `AVAILABLE` | 환불 |
| `block()` | `AVAILABLE` | `BLOCKED` | 판매 제외(시야 제한석·관계자석 등) |
| `unblock()` | `BLOCKED` | `AVAILABLE` | 판매 제외 해제 |
