# 조회

> 상위: [API 레퍼런스 인덱스](./README.md)

티켓 이벤트 단건/목록 조회 엔드포인트를 다룬다. 두 엔드포인트 모두 인증(Bearer)이 필요하며,
응답 본문은 `TicketEventResponse` 형식이다.

---

## 단건 조회

`GET /api/ticket-events/{id}` → **200 OK**

```json
// 200 OK
{
  "id": 1,
  "ticketEventName": "2026 여름 콘서트",
  "ticketOpenAt": "2026-07-01T00:00:00Z",
  "ticketClosedAt": "2026-07-10T00:00:00Z",
  "ticketEventAt": "2026-07-20T19:00:00Z",
  "ticketEventStatus": "OPEN",
  "ticketCreationStatus": "COMPLETED",
  "ticketEventCategory": "CONCERT",
  "createdAt": "2026-06-07T12:00:00Z",
  "updatedAt": "2026-06-07T12:30:00Z"
}
```

- 대상이 없으면 **404**(`TicketEventNotFound`) → [errors.md](./errors.md).

---

## 목록 조회 (필터)

`GET /api/ticket-events?category=&status=` → **200 OK**

`TicketEventResponse` 배열을 반환한다.

### 필터 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `category` | `TicketEventCategory` | 선택 | 카테고리로 필터(미지정 시 전체) |
| `status` | `TicketEventStatus` | 선택 | 예매 상태로 필터(미지정 시 전체) |

두 파라미터 모두 선택이며, 미지정 시 해당 조건을 적용하지 않는다. 값은
[Enum 정의](../domain-layer/enums.md)의 이름과 정확히 일치해야 한다.

```json
// GET /api/ticket-events?category=CONCERT&status=OPEN → 200 OK
[
  {
    "id": 1,
    "ticketEventName": "2026 여름 콘서트",
    "ticketEventStatus": "OPEN",
    "ticketCreationStatus": "COMPLETED",
    "ticketEventCategory": "CONCERT"
  }
]
```

- 조건에 맞는 이벤트가 없으면 빈 배열 `[]` 을 **200** 으로 반환한다.
