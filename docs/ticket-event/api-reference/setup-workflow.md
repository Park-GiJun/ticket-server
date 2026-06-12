# 셋업 워크플로우

> 상위: [API 레퍼런스 인덱스](./README.md)

티켓 이벤트는 **순서대로** 기본 정보 → 구역 → 좌석을 구성한 뒤 완료한다. 셋업 단계는
`ticketCreationStatus` 로 추적되며 **순방향으로만** 진행된다. 단계를 어긴 호출은 **409**
(`InvalidStatusTransition`)로 거부된다.

```
EVENT_CREATED → SECTION_CREATED → SEAT_CREATED → COMPLETED
   (1단계)          (2단계)          (3단계)        (4단계)
```

`ticketCreationStatus` 의 의미는 [도메인 enum 정의](../domain-layer/enums.md)를 참고한다.

---

## 생성 (셋업 1단계)

`POST /api/ticket-events` → **201 Created**

```json
// Request
{
  "ticketEventName": "2026 여름 콘서트",
  "ticketOpenAt": "2026-07-01T00:00:00Z",
  "ticketClosedAt": "2026-07-10T00:00:00Z",
  "ticketEventAt": "2026-07-20T19:00:00Z",
  "ticketEventCategory": "CONCERT"
}
```
```json
// 201 Created
{
  "id": 1,
  "ticketEventName": "2026 여름 콘서트",
  "ticketOpenAt": "2026-07-01T00:00:00Z",
  "ticketClosedAt": "2026-07-10T00:00:00Z",
  "ticketEventAt": "2026-07-20T19:00:00Z",
  "ticketEventStatus": "SCHEDULED",
  "ticketCreationStatus": "EVENT_CREATED",
  "ticketEventCategory": "CONCERT",
  "createdAt": "2026-06-07T12:00:00Z",
  "updatedAt": "2026-06-07T12:00:00Z"
}
```

### 검증 규칙

- `ticketEventName`: 필수, 공백 불가(`@NotBlank`), 최대 100자(`@Size(max = 100)`).
- `ticketOpenAt` · `ticketClosedAt` · `ticketEventAt`: 필수(`@NotNull`), ISO-8601 `Instant`.
- `ticketEventCategory`: 필수(`@NotNull`).
- 도메인 불변식: `ticketClosedAt > ticketOpenAt`, `ticketEventAt ≥ ticketClosedAt`. 위반 시 **400**.

검증 실패(`@Valid`)와 도메인 불변식 위반(`IllegalArgumentException`)은 모두 **400** 으로
매핑된다 → [errors.md](./errors.md).

초기 예매 상태는 `SCHEDULED`, 초기 셋업 단계는 `EVENT_CREATED`.

---

## 구역 생성 (셋업 2단계)

`POST /api/ticket-events/{id}/sections` → **201 Created**

셋업 단계 `EVENT_CREATED → SECTION_CREATED` 로 전이한다.

```json
// Request
{
  "sections": [
    { "sectionName": "VIP석", "grade": "VIP", "price": 198000, "capacity": 100 },
    { "sectionName": "1층 R석", "grade": "R", "price": 132000, "capacity": 300 }
  ]
}
```
```json
// 201 Created
{
  "ticketEvent": {
    "id": 1,
    "ticketEventName": "2026 여름 콘서트",
    "ticketEventStatus": "SCHEDULED",
    "ticketCreationStatus": "SECTION_CREATED",
    "ticketEventCategory": "CONCERT",
    "createdAt": "2026-06-07T12:00:00Z",
    "updatedAt": "2026-06-07T12:05:00Z"
  },
  "sections": [
    { "id": 1, "ticketEventId": 1, "sectionName": "VIP석", "grade": "VIP", "price": 198000, "capacity": 100 },
    { "id": 2, "ticketEventId": 1, "sectionName": "1층 R석", "grade": "R", "price": 132000, "capacity": 300 }
  ]
}
```

> 응답은 `SectionCreationResponse` = 갱신된 `ticketEvent`(전체 `TicketEventResponse` 필드) + 생성된 `sections` 배열.

### 검증 규칙 (각 `sections[]` 항목)

- `sections`: 비어 있지 않음(`@NotEmpty`), 각 항목 `@Valid`.
- `sectionName`: 공백 불가(`@NotBlank`), 최대 100자.
- `grade`: 공백 불가(`@NotBlank`), 최대 30자.
- `price`: 0 이상(`@PositiveOrZero`).
- `capacity`: 1 이상(`@Min(1)`).

응답 코드: 201 / 400(검증 실패) / 404(이벤트 없음) / 409(생성 단계 전이 위반).

---

## 좌석 생성 (셋업 3단계)

`POST /api/ticket-events/{id}/seats` (본문 없음) → **201 Created**

셋업 단계 `SECTION_CREATED → SEAT_CREATED` 로 전이한다. 각 구역의 `capacity` 만큼 좌석을
자동 생성하며, 좌석의 `ticketEventId` 는 소속 구역 값을 복사한다.

```json
// 201 Created
{
  "ticketEvent": {
    "id": 1,
    "ticketEventStatus": "SCHEDULED",
    "ticketCreationStatus": "SEAT_CREATED",
    "ticketEventCategory": "CONCERT"
  },
  "createdSeatCount": 400
}
```

> 응답은 `SeatCreationResponse` = 갱신된 `ticketEvent` + `createdSeatCount`(생성된 좌석 수).

- 구역이 하나도 없으면 **404**(`SectionNotFound`).

응답 코드: 201 / 404(이벤트·구역 없음) / 409(생성 단계 전이 위반).

---

## 셋업 완료 (4단계)

`POST /api/ticket-events/{id}/complete` (본문 없음) → **200 OK**

셋업 단계 `SEAT_CREATED → COMPLETED` 로 전이한다. 응답은 갱신된 `TicketEventResponse`.

```json
// 200 OK
{
  "id": 1,
  "ticketEventStatus": "SCHEDULED",
  "ticketCreationStatus": "COMPLETED",
  "ticketEventCategory": "CONCERT"
}
```

응답 코드: 200 / 404(이벤트 없음) / 409(생성 단계 전이 위반).

셋업이 `COMPLETED` 가 되면 예매 오픈이 가능하다 → [예매 상태 전이](./status-transition.md).
