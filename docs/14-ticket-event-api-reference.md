# 14. 티켓 이벤트 — API 레퍼런스

베이스 경로: `/api/ticket-events`

> 모든 엔드포인트는 **인증 필요(Bearer)**. 현재 `SecurityConfig` 의 기본 정책상
> `/api/auth/**`·Swagger 외 경로는 유효한 JWT 를 요구한다. (조회 공개 전환이나 ADMIN 전용
> 제한은 추후 `SecurityConfig`/메서드 보안으로 조정 → [07](./07-security-and-jwt.md))

## 엔드포인트 요약

| Method | Path | 설명 | 인증 | 성공 코드 |
|--------|------|------|------|----------|
| POST | `/api/ticket-events` | 생성 | **Bearer** | 201 |
| PUT | `/api/ticket-events/{id}` | 수정(이름/일정/카테고리) | **Bearer** | 200 |
| POST | `/api/ticket-events/{id}/open` | 예매 오픈 | **Bearer** | 200 |
| POST | `/api/ticket-events/{id}/close` | 예매 마감 | **Bearer** | 200 |
| POST | `/api/ticket-events/{id}/cancel` | 취소 | **Bearer** | 200 |
| GET | `/api/ticket-events/{id}` | 단건 조회 | **Bearer** | 200 |
| GET | `/api/ticket-events?category=&status=` | 목록 조회(필터) | **Bearer** | 200 |

`category` ∈ `CONCERT`/`MUSICAL`/`PLAY`/`SPORTS`/`EXHIBITION`/`FESTIVAL`/`ETC`,
`status` ∈ `SCHEDULED`/`OPEN`/`CLOSED`/`SOLD_OUT`/`CANCELLED`/`COMPLETED` → [11](./11-ticket-event-domain-layer.md)

---

## 생성

`POST /api/ticket-events`

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
  "ticketEventCategory": "CONCERT",
  "createdAt": "2026-06-07T12:00:00Z",
  "updatedAt": "2026-06-07T12:00:00Z"
}
```

- 검증: `ticketEventName`(최대 100자, 공백 불가), 세 시각·`ticketEventCategory` 필수.
- 도메인 불변식: `ticketClosedAt > ticketOpenAt`, `ticketEventAt ≥ ticketClosedAt`. 위반 시 **400**.
- 초기 상태는 항상 `SCHEDULED`.

## 수정

`PUT /api/ticket-events/{id}`

요청 본문은 생성과 동일(이름/일정/카테고리). **상태는 변경되지 않으며**(open/close/cancel 로만 전이),
`createdAt` 은 보존된다. 대상이 없으면 **404**.

## 상태 전이

`POST /api/ticket-events/{id}/open` · `/close` · `/cancel` (본문 없음)

| 액션 | 허용 출발 상태 | 결과 상태 |
|------|----------------|-----------|
| `open` | `SCHEDULED` | `OPEN` |
| `close` | `OPEN` | `CLOSED` |
| `cancel` | `SCHEDULED`/`OPEN`/`CLOSED`/`SOLD_OUT` | `CANCELLED` |

- 성공 시 **200** 과 갱신된 티켓 이벤트 반환.
- 허용되지 않는 전이는 **409**(`InvalidStatusTransition`), 대상 없음은 **404**.

```json
// POST /api/ticket-events/1/open → 200 OK
{ "id": 1, "ticketEventStatus": "OPEN", "ticketEventName": "2026 여름 콘서트", ... }
```

## 단건 조회

`GET /api/ticket-events/{id}` → **200**(생성 응답과 동일 형식), 없으면 **404**.

## 목록 조회

`GET /api/ticket-events?category=CONCERT&status=OPEN`

- 두 파라미터 모두 선택. 미지정 시 전체 조회.
- **200** 과 티켓 이벤트 배열 반환.

```json
// 200 OK
[
  { "id": 1, "ticketEventName": "2026 여름 콘서트", "ticketEventStatus": "OPEN",
    "ticketEventCategory": "CONCERT", ... }
]
```

---

## 에러 응답 포맷

User 와 동일하게 `GlobalExceptionHandler` 가 일관된 형식으로 반환한다 → [09](./09-api-reference.md#에러-응답-포맷).

### 도메인 예외 → HTTP 매핑

| 예외 | HTTP |
|------|------|
| `TicketEventNotFound` | 404 Not Found |
| `InvalidStatusTransition` | 409 Conflict |
| 도메인 불변식 위반(`IllegalArgumentException`) / Bean Validation 실패 | 400 Bad Request |
