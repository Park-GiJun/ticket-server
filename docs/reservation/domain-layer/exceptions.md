# 도메인 예외

> 상위: [도메인 레이어 인덱스](./README.md)

`domain/exception/ReservationException.kt` — `sealed` 로 두어 예외 처리기에서 `when` 으로 망라한다.

```kotlin
sealed class ReservationException(message: String) : RuntimeException(message) {
    class ReservationNotFound : ReservationException("예매를 찾을 수 없습니다")
    class InvalidStatusTransition(reason: String) : ReservationException(reason)
    class SeatUnavailable(reason: String) : ReservationException(reason)
    class ReservationExpired : ReservationException("점유 시한이 만료된 예매입니다")
}
```

| 예외 | 발생 상황 | 메시지 |
|------|-----------|--------|
| `ReservationNotFound` | 식별자/예매 코드에 해당하는 예매가 없음 | "예매를 찾을 수 없습니다" |
| `InvalidStatusTransition` | 허용되지 않은 상태 전이([`ReservationModel`](./reservation-model.md)) | `reason` (전이 메서드가 동적 생성) |
| `SeatUnavailable` | 점유 요청 시 좌석이 이미 점유/매진 — `REJECTED` 의 도메인 사유 | `reason` |
| `ReservationExpired` | 만료된(`EXPIRED`) 예매에 결제/확정을 시도 | "점유 시한이 만료된 예매입니다" |

> 생성 불변식 위반은 `init` 의 `require` 가 던지는 `IllegalArgumentException` 이며, 이 sealed 계층에 속하지 않는다.

> **saga 맥락 주의**: 서비스 경계를 넘는 실패(다른 서비스 응답·이벤트로 알게 되는 실패)는
> 호출 측에서 예외를 던지기보다 **보상 흐름**으로 처리한다(예: 좌석 점유 실패 → `reject()` 로 상태 전이).
> 위 예외들은 reservation-service **내부**의 동기 처리(조회·전이 검증·결제 확정 시점 검증)에서 쓰인다.

HTTP 매핑은 API 레퍼런스(예정)에서 정의한다.
