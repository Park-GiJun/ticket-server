# 조회

> 상위: [API 레퍼런스 인덱스](./README.md)

티켓 이벤트·구역(Section)·좌석(Seat)의 조회 엔드포인트를 다룬다. 모두 인증(Bearer)이 필요하다.
이벤트 응답은 `TicketEventResponse`, 구역/좌석은 각각 `TicketEventSectionResponse` /
`TicketEventSeatResponse` 형식이다.

- 이벤트 조회는 `TicketEventWebAdapter`, 구역/좌석 조회는 각각 `TicketEventSectionWebAdapter` /
  `TicketEventSeatWebAdapter` 가 담당한다(쓰기/셋업은 `TicketEventWebAdapter`).
- 구역/좌석은 모두 **이벤트 하위 중첩 경로**(`/api/ticket-events/{eventId}/...`)로 노출된다.
  단건 조회는 경로의 `eventId` 소속을 검증한다(아래 [소속 검증](#소속-검증) 참고).

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

---

## 구역(Section) 조회

`TicketEventSectionWebAdapter` 가 담당한다. 응답 본문은 `TicketEventSectionResponse`
(`id` / `ticketEventId` / `sectionName` / `grade` / `price` / `capacity`).

### 구역 목록 조회

`GET /api/ticket-events/{eventId}/sections` → **200 OK**

해당 이벤트에 속한 구역의 `TicketEventSectionResponse` 배열을 반환한다(없으면 `[]`).

```json
// 200 OK
[
  { "id": 10, "ticketEventId": 1, "sectionName": "VIP석", "grade": "VIP", "price": 150000, "capacity": 100 },
  { "id": 11, "ticketEventId": 1, "sectionName": "R석",  "grade": "R",   "price": 110000, "capacity": 200 }
]
```

### 구역 단건 조회

`GET /api/ticket-events/{eventId}/sections/{sectionId}` → **200 OK**

- 구역이 없거나 **`eventId` 소속이 아니면 404**(`SectionNotFound`).

---

## 좌석(Seat) 조회

`TicketEventSeatWebAdapter` 가 담당한다. 좌석 응답 본문은 `TicketEventSeatResponse`
(`id` / `sectionId` / `ticketEventId` / `rowLabel` / `seatNumber` / `status`).

### 좌석 목록 조회

`GET /api/ticket-events/{eventId}/seats` → **200 OK**

해당 이벤트의 좌석 `TicketEventSeatResponse` 배열을 반환한다(없으면 `[]`).

```json
// 200 OK
[
  { "id": 100, "sectionId": 10, "ticketEventId": 1, "rowLabel": "", "seatNumber": 1, "status": "AVAILABLE" },
  { "id": 101, "sectionId": 10, "ticketEventId": 1, "rowLabel": "", "seatNumber": 2, "status": "SOLD" }
]
```

> 한 이벤트의 좌석은 수천 건일 수 있으므로, 페이지네이션/구역별 필터는 추후 도입 예정이다(현재는 전체 반환).

### 좌석 잔여 현황 조회

`GET /api/ticket-events/{eventId}/seats/availability` → **200 OK**

좌석을 상태별로 집계해 합계/잔여석과 함께 반환한다. 응답 본문은 `SeatAvailabilityResponse`.

```json
// 200 OK
{
  "ticketEventId": 1,
  "counts": { "AVAILABLE": 80, "HELD": 0, "SOLD": 20, "BLOCKED": 0 },
  "total": 100,
  "available": 80
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `counts` | `Map<SeatStatus, Long>` | 상태별 좌석 수. 집계에 없는 상태도 **0 으로 채워 4개 상태를 항상 포함** |
| `total` | `Long` | 전체 좌석 수(상태별 합) |
| `available` | `Long` | 예매 가능(`AVAILABLE`) 좌석 수 |

- 집계의 단일 진실 원천은 좌석 상태다. 고빈도 잔여석 조회는 추후 Redis 읽기 모델로 분리할 수 있다.

### 좌석 단건 조회

`GET /api/ticket-events/{eventId}/seats/{seatId}` → **200 OK**

- 좌석이 없거나 **`eventId` 소속이 아니면 404**(`SeatNotFound`).
- 경로상 `/seats/availability` 는 리터럴이라 `/seats/{seatId}` 보다 우선 매칭되므로 충돌하지 않는다.

---

## 소속 검증

구역/좌석 **단건 조회**는 경로의 `{eventId}` 가 해당 리소스의 실제 소속과 일치하는지 검증한다.
다른 이벤트의 id 로 조회하면 (리소스가 존재하더라도) **404**(`SectionNotFound` / `SeatNotFound`)
를 반환한다 — "그 이벤트 하위에 그 리소스가 없다"는 의미이며, 리소스 존재 여부 노출도 막는다.
좌석/구역 모델이 `ticketEventId` 를 보유(비정규화)하므로 추가 조회 없이 검증한다.
