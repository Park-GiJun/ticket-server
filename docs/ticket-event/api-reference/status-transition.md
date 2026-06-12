# 예매 상태 전이

> 상위: [API 레퍼런스 인덱스](./README.md)

`ticketEventStatus`(예매 진행 상태)를 전이시키는 엔드포인트와, 기본 정보를 고치는
수정(PUT) 엔드포인트를 다룬다. 셋업 단계(`ticketCreationStatus`) 전이는
[셋업 워크플로우](./setup-workflow.md)를 참고한다.

전이는 모두 도메인 모델(`TicketEventModel`)의 `transitionTo` 가 허용 출발 상태를 검사하며,
허용되지 않는 전이는 `InvalidStatusTransition` → **409** 로 매핑된다.

---

## 상태 전이 엔드포인트

`POST /api/ticket-events/{id}/open` · `/close` · `/cancel` (모두 본문 없음) → **200 OK**

| 액션 | 엔드포인트 | 허용 출발 상태 | 결과 상태 |
|------|------------|----------------|-----------|
| `open` | `POST /{id}/open` | `SCHEDULED` | `OPEN` |
| `close` | `POST /{id}/close` | `OPEN`, `SOLD_OUT` | `CLOSED` |
| `cancel` | `POST /{id}/cancel` | `SCHEDULED`, `OPEN`, `CLOSED`, `SOLD_OUT` | `CANCELLED` |

성공 시 갱신된 `TicketEventResponse` 를 **200** 으로 반환한다.

```json
// POST /api/ticket-events/1/open → 200 OK
{
  "id": 1,
  "ticketEventName": "2026 여름 콘서트",
  "ticketEventStatus": "OPEN",
  "ticketCreationStatus": "COMPLETED",
  "ticketEventCategory": "CONCERT"
}
```

### 응답 코드

- **200** — 전이 성공.
- **404** — 대상 티켓 이벤트 없음(`TicketEventNotFound`).
- **409** — 허용되지 않는 상태 전이(`InvalidStatusTransition`).

자세한 매핑은 [errors.md](./errors.md)를 참고한다.

---

## 내부 전이 (엔드포인트 없음)

매진(`markSoldOut`)·매진 해제(`reopen`)는 **REST 엔드포인트가 없다.** 좌석 판매/환불 흐름에서
도메인 내부적으로 일어난다.

| 내부 전이 | 허용 출발 상태 | 결과 상태 | 트리거 |
|-----------|----------------|-----------|--------|
| `markSoldOut` | `OPEN` | `SOLD_OUT` | 잔여(AVAILABLE) 좌석이 0 이 되는 마지막 판매 시점 |
| `reopen` | `SOLD_OUT` | `OPEN` | 환불 등으로 좌석이 다시 풀릴 때 |

좌석 카운트는 좌석 상태(단일 진실 원천)에서 파생하며 이벤트 모델은 보유하지 않는다.
좌석 상태 전이 규칙은 [도메인 enum 정의](../domain-layer/enums.md)를 참고한다.

---

## 수정

`PUT /api/ticket-events/{id}` → **200 OK**

요청 본문은 생성과 동일하다(이름/일정/카테고리). 요청·검증 규칙은
[셋업 워크플로우 — 생성](./setup-workflow.md#생성-셋업-1단계)과 같다.

- **상태/셋업 단계는 변경되지 않으며**, `createdAt` 은 보존된다.
- 도메인 불변식(`ticketClosedAt > ticketOpenAt`, `ticketEventAt ≥ ticketClosedAt`) 위반 시 **400**.
- 대상이 없으면 **404**(`TicketEventNotFound`).

응답은 갱신된 `TicketEventResponse`. 응답 코드: 200 / 400(검증·불변식) / 404(이벤트 없음).
