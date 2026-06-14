# 에러 레퍼런스

> 상위: [API 레퍼런스 인덱스](./README.md)

티켓 이벤트 API 의 에러 응답 포맷과 도메인 예외 → HTTP 상태 매핑을 다룬다.

---

## 에러 응답 포맷

모든 서비스가 공유하는 표준 에러 바디(`ApiErrorResponse`)를 반환한다.

```json
{
  "status": 404,
  "message": "티켓 이벤트를 찾을 수 없습니다",
  "fieldErrors": {}
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `status` | int | HTTP 상태 코드 |
| `message` | string | 사람이 읽을 수 있는 에러 메시지 |
| `fieldErrors` | object | 필드별 검증 메시지(검증 실패 시에만 채워짐, 기본 `{}`) |

Bean Validation(`@Valid`) 실패 시 `CommonExceptionHandler` 가 `fieldErrors` 를 채워
**400** 으로 반환한다.

```json
// 400 Bad Request — 요청 값 검증 실패
{
  "status": 400,
  "message": "요청 값이 올바르지 않습니다",
  "fieldErrors": {
    "ticketEventName": "공백일 수 없습니다",
    "sections[0].capacity": "1 이상이어야 합니다"
  }
}
```

---

## 도메인 예외 → HTTP 매핑

`TicketEventExceptionHandler` 가 `sealed class TicketEventException` 을 `when` 으로 망라해
매핑한다(아래 표는 핸들러의 `when` 분기와 1:1 일치).

| 예외 | HTTP | 비고 |
|------|------|------|
| `TicketEventNotFound` | **404** Not Found | 대상 티켓 이벤트 없음 |
| `SectionNotFound` | **404** Not Found | 좌석 구역 없음(단건 조회 시 경로 `eventId` 소속 불일치 포함) |
| `SeatNotFound` | **404** Not Found | 좌석 없음(단건 조회 시 경로 `eventId` 소속 불일치 포함) |
| `InvalidStatusTransition` | **409** Conflict | 허용되지 않는 예매/셋업 단계 전이 |
| `InvalidSeatStatusTransition` | **409** Conflict | 허용되지 않는 좌석 상태 전이 |

공통 예외(`CommonExceptionHandler`)는 도메인과 무관하게 다음을 처리한다.

| 예외 | HTTP | 비고 |
|------|------|------|
| `MethodArgumentNotValidException` (`@Valid` 검증 실패) | **400** Bad Request | `fieldErrors` 채워짐 |
| `IllegalArgumentException` (도메인 불변식 `require` 위반) | **400** Bad Request | 일정 불변식 위반 등 |

> 정리: NotFound 계열 → **404**, InvalidTransition 계열 → **409**, 검증 실패·불변식 위반 → **400**.
