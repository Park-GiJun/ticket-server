# enum 들

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/enums/` — 다른 도메인과 동일한 위치 규칙.

## ReservationStatus

`domain/enums/ReservationStatus.kt` — **예매 1건의 생명주기 상태**.
좌석 점유·결제가 서비스 간 saga 로 진행되므로, 점유 확정 전 `PENDING` 과 실패 종료 `REJECTED` 를 둔다.

```kotlin
enum class ReservationStatus {
    PENDING, HELD, CONFIRMED, EXPIRED, CANCELLED, REJECTED,
}
```

| 값 | 의미 | 종료 상태 | 대응 좌석 상태(ticket-event) |
|----|------|-----------|------------------------------|
| `PENDING` | 예매 생성됨, 좌석 점유 명령 진행 중(saga in-flight) | — | (점유 시도 중) |
| `HELD` | 좌석 점유 확정, 결제 대기(만료 시한 있음) | — | `HELD` |
| `CONFIRMED` | 결제 완료, 예매 확정 | ✓ | `SOLD` |
| `EXPIRED` | 점유 시한 초과로 자동 만료 | ✓ | `AVAILABLE`(보상) |
| `CANCELLED` | 사용자 취소 / 환불 | ✓ | `AVAILABLE`(보상) |
| `REJECTED` | 좌석 점유 실패(이미 점유·매진 등)로 saga 종료 | ✓ | 변화 없음 |

> 상태 전이표는 [reservation-model.md](./reservation-model.md#예매-상태-전이-reservationstatus) 참고.
> 좌석 상태(`SeatStatus`) 정의는 [ticket-event enums](../../ticket-event/domain-layer/enums.md#seatstatus) 참고.
