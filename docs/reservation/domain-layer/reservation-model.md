# ReservationModel

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/model/ReservationModel.kt`

사용자의 **티켓 예매 1건**. 정확히 **하나의 좌석**을 점유하며,
좌석 점유·결제·취소는 **다른 서비스와의 saga**(Kafka 이벤트 오케스트레이션 + OpenFeign)로 진행된다.
그 결과가 이 모델의 상태(`PENDING → HELD → CONFIRMED`, 실패/만료/취소 종료)로 반영된다.

> ⚠️ 설계 초안(design-first). `reservation-service` 에는 아직 도메인 코드가 없고, 이 문서가 구현보다 앞선다.
> 도메인 레이어는 **상태 머신과 불변식만** 담는다. saga 실행(Feign 클라이언트·Kafka 토픽·outbox)은
> 애플리케이션/인프라 레이어 책임이며 별도 문서로 추가한다.

```kotlin
data class ReservationModel(
    val id: Long? = null,
    val reservationCode: String,        // 외부 노출용 식별자(UUID 등). 내부 PK(id)와 분리
    val transactionId: String,          // saga 상관관계 ID — 서비스 간 이벤트 추적/멱등성 키
    val userId: Long,                   // 예매자 (게이트웨이 X-User-Id 출처)
    val ticketEventId: Long,            // 소속 이벤트(비정규화) — 이벤트 단위 핫쿼리/파티셔닝용
    val seatId: Long,                   // 예매 대상 좌석 (1 예매 = 1 좌석)
    val status: ReservationStatus = ReservationStatus.PENDING,
    val price: Long,                    // 예매 시점 확정 가격 스냅샷(구역 가격을 복사)
    val holdExpiresAt: Instant,         // 점유 만료 시각(HELD 상태에서만 의미)
    val paymentId: String? = null,      // 결제 확정 시 채워지는 결제/거래 식별자
    val confirmedAt: Instant? = null,   // 결제 확정 시각
    val failureReason: String? = null,  // REJECTED 사유(좌석 점유 실패 등)
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(reservationCode.isNotBlank()) { "reservationCode must not be blank" }
        require(transactionId.isNotBlank()) { "transactionId must not be blank" }
        require(price >= 0) { "price must not be negative" }
    }

    fun isPending(): Boolean = status == ReservationStatus.PENDING
    fun isHeld(): Boolean = status == ReservationStatus.HELD
    fun isExpired(at: Instant): Boolean = isHeld() && !at.isBefore(holdExpiresAt)
}
```

- `reservationCode` — 외부(URL·결제 연동)에 노출하는 식별자. 내부 PK(`id`)를 노출하지 않기 위해 분리한다.
- `transactionId` — **saga 상관관계 ID**. 예매 생성 시 발급해, 좌석 점유/결제/보상 이벤트를 한 흐름으로 묶고
  소비자 측 **멱등 처리**의 키로 쓴다(중복 이벤트 방어). [enums.md](./enums.md)·오케스트레이션 문서(예정)와 연결.
- `userId` — 예매자. 게이트웨이가 보장한 `X-User-Id` 헤더에서 온다(서비스는 헤더를 신뢰).
- `ticketEventId` — 소속 [이벤트](../../ticket-event/domain-layer/ticket-event-model.md) 식별자.
  `seatId → seat.ticketEventId` 로 도달 가능하지만 **이벤트 단위 조회/집계**를 위해 의도적으로 중복 보유한다
  ([seat-model 의 비정규화 근거](../../ticket-event/domain-layer/seat-model.md#ticketeventid-비정규화-근거)와 동일 맥락).
- `seatId` — 점유 대상 [좌석](../../ticket-event/domain-layer/seat-model.md). **예매 1건은 좌석 1개**.
  좌석 상태(`AVAILABLE`/`HELD`/`SOLD`)는 **ticket-event-service 가 소유**한다(단일 진실 원천).
- `price` — 예매 시점의 가격을 **스냅샷**으로 보관한다(구역 가격이 나중에 바뀌어도 예매 가격은 불변).
- `paymentId` / `confirmedAt` — `confirm()`(결제 확정) 시에만 채워진다.
- `failureReason` — `reject()` 로 실패 종료될 때의 사유.
- **생성 불변식(`init`)**: `reservationCode`·`transactionId` 공백 금지, `price >= 0`.
- `isExpired(at)` — `HELD` 이면서 주어진 시각이 만료 시각 이상인지(만료 스윕/조회용).
- `id == null` 은 아직 영속화되지 않은 신규 모델을 의미한다.

## 예매 상태 전이 (`ReservationStatus`)

`transitionTo(target, allowedFrom)` 헬퍼가 허용 출발 상태를 검증하며, 위반 시
[`ReservationException.InvalidStatusTransition`](./exceptions.md) 을 던진다.
`ReservationStatus` 값 정의는 [enums.md](./enums.md) 참고.

```
                ┌──────────── reject() ───────────► REJECTED   (좌석 점유 실패)
                │
  PENDING ──── markHeld() ──► HELD ──── confirm() ──► CONFIRMED
                                │                          │
                                ├──── expire() ──► EXPIRED  │
                                │                           │
                                └──────── cancel() ◄────────┘ ► CANCELLED
```

| 메서드 | 허용 출발 | 결과 | 트리거(오케스트레이션) | ticket-event 좌석 전이 |
|--------|-----------|------|------------------------|------------------------|
| `markHeld()` | `PENDING` | `HELD` | `SeatHeld` 이벤트 수신 | `AVAILABLE → HELD` (점유 명령 성공) |
| `reject(reason)` | `PENDING` | `REJECTED` | `SeatHoldFailed` 이벤트 | 없음(점유 실패라 보상 불필요) |
| `confirm(paymentId, at)` | `HELD` | `CONFIRMED` | `PaymentCompleted` 이벤트 | `HELD → SOLD` |
| `expire()` | `HELD` | `EXPIRED` | 점유 시한 초과(타임아웃) | `HELD → AVAILABLE` (보상) |
| `cancel()` | `HELD`, `CONFIRMED` | `CANCELLED` | 사용자 취소 / 환불 | `HELD → AVAILABLE`, `SOLD → AVAILABLE`(보상) |

> **좌석 전이는 ticket-event-service 가 수행**한다. reservation 의 상태 전이는 그 결과(이벤트)를 반영하거나
> 보상 명령을 촉발할 뿐, 이 도메인이 직접 좌석을 바꾸지 않는다(서비스 경계 = 트랜잭션 경계).
> 따라서 두 서비스의 정합성은 로컬 트랜잭션이 아니라 **saga + 보상**으로 보장한다.
> 매진(`SOLD_OUT`) 자동 전이는 좌석 잔여 집계가 0이 되는 시점에 ticket-event-service 에서 처리한다.

## 오케스트레이션 개요 (도메인 바깥)

도메인은 상태 머신만 정의하고, 실제 흐름 제어는 애플리케이션/인프라 레이어가 담당한다(문서 예정).

- **OpenFeign(동기)**: 예매 생성 시 좌석/가격 조회 등 **즉시 응답이 필요한 읽기**에 사용.
- **Kafka(비동기 오케스트레이션)**: 좌석 점유 → 결제 → 확정/보상의 **상태 변경 흐름**을 이벤트로 구동.
  발행은 **Transactional Outbox** 로 DB 커밋과 원자화하고, 소비는 `transactionId` 기준 **멱등** 처리한다.

## 열어둔 설계 결정

- **오케스트레이션 방식**: 중앙 orchestrator(saga 상태 머신) vs choreography(서비스 간 이벤트 연쇄) — 인프라 레이어에서 확정.
- **만료 처리 주체**: 스케줄러 스윕 vs Redis TTL 만료 이벤트.
- **동시성 제어**: 같은 좌석 동시 점유 방지는 ticket-event-service 의 좌석 점유 지점(비관적 락/낙관적 버전)에서 보장.
- **결제 연동 경계**: `paymentId` 가 가리키는 결제 도메인의 형태(별도 서비스 여부)와 다좌석 묶음(주문) 상위 개념은 미정.
