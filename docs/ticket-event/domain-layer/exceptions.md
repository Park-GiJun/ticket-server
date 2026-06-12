# 도메인 예외

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/exception/TicketEventException.kt` — `sealed` 로 두어 예외 처리기에서 `when` 으로 망라한다.

```kotlin
sealed class TicketEventException(message: String) : RuntimeException(message) {
    class TicketEventNotFound : TicketEventException("티켓 이벤트를 찾을 수 없습니다")
    class InvalidStatusTransition(reason: String) : TicketEventException(reason)
    class SectionNotFound : TicketEventException("좌석 구역을 찾을 수 없습니다")
    class SeatNotFound : TicketEventException("좌석을 찾을 수 없습니다")
    class InvalidSeatStatusTransition(reason: String) : TicketEventException(reason)
}
```

| 예외 | 발생 상황 | 메시지 |
|------|-----------|--------|
| `TicketEventNotFound` | 식별자에 해당하는 이벤트가 없음 | "티켓 이벤트를 찾을 수 없습니다" |
| `InvalidStatusTransition` | 예매 상태 또는 셋업 단계의 허용되지 않은 전이([`TicketEventModel`](./ticket-event-model.md)) | `reason` (전이 메서드가 동적 생성) |
| `SectionNotFound` | 식별자에 해당하는 구역이 없음 | "좌석 구역을 찾을 수 없습니다" |
| `SeatNotFound` | 식별자에 해당하는 좌석이 없음 | "좌석을 찾을 수 없습니다" |
| `InvalidSeatStatusTransition` | 허용되지 않은 좌석 상태 전이([`TicketEventSeatModel`](./seat-model.md)) | `reason` (전이 메서드가 동적 생성) |

> 생성 불변식 위반은 `init` 의 `require` 가 던지는 `IllegalArgumentException` 이며,
> 이 sealed 계층에 속하지 않는다.

HTTP 매핑은 [API 레퍼런스 — 도메인 예외/HTTP 매핑](../api-reference/errors.md) 참고.
