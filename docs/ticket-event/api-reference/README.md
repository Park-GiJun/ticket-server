# 티켓 이벤트 — API 레퍼런스

베이스 경로: `/api/ticket-events`

> 모든 엔드포인트는 **인증 필요(Bearer)**. 현재 `SecurityConfig` 의 기본 정책상
> `/api/auth/**`·Swagger 외 경로는 유효한 JWT 를 요구한다. 조회 공개 전환이나 ADMIN 전용
> 제한은 추후 `SecurityConfig`/메서드 보안으로 조정한다.

컨트롤러(`TicketEventWebAdapter`)는 모든 응답을 `TicketEventResponse`(또는 이를 감싼
`SectionCreationResponse`/`SeatCreationResponse`) 형태로 반환한다. 예매 진행 상태
(`ticketEventStatus`)와 셋업 단계(`ticketCreationStatus`)는 별개의 필드다.

## 엔드포인트 요약

| Method | Path | 설명 | 인증 | 성공 코드 | 세부 문서 |
|--------|------|------|------|-----------|-----------|
| POST | `/api/ticket-events` | 생성 (셋업 1단계) | **Bearer** | 201 | [셋업 워크플로우](./setup-workflow.md) |
| PUT | `/api/ticket-events/{id}` | 수정(이름/일정/카테고리) | **Bearer** | 200 | [상태 전이](./status-transition.md) |
| POST | `/api/ticket-events/{id}/sections` | 구역 생성 (셋업 2단계) | **Bearer** | 201 | [셋업 워크플로우](./setup-workflow.md) |
| POST | `/api/ticket-events/{id}/seats` | 좌석 생성 (셋업 3단계) | **Bearer** | 201 | [셋업 워크플로우](./setup-workflow.md) |
| POST | `/api/ticket-events/{id}/complete` | 셋업 완료 (4단계) | **Bearer** | 200 | [셋업 워크플로우](./setup-workflow.md) |
| POST | `/api/ticket-events/{id}/open` | 예매 오픈 | **Bearer** | 200 | [상태 전이](./status-transition.md) |
| POST | `/api/ticket-events/{id}/close` | 예매 마감 | **Bearer** | 200 | [상태 전이](./status-transition.md) |
| POST | `/api/ticket-events/{id}/cancel` | 취소 | **Bearer** | 200 | [상태 전이](./status-transition.md) |
| GET | `/api/ticket-events/{id}` | 단건 조회 | **Bearer** | 200 | [조회](./queries.md) |
| GET | `/api/ticket-events?category=&status=` | 목록 조회(필터) | **Bearer** | 200 | [조회](./queries.md) |

총 10개 엔드포인트. 에러 응답 포맷과 예외 매핑은 [에러 레퍼런스](./errors.md)를 참고한다.

## Enum 값

- `ticketEventCategory` ∈ `CONCERT` / `MUSICAL` / `PLAY` / `SPORTS` / `EXHIBITION` / `FESTIVAL` / `ETC`
- `ticketEventStatus` ∈ `SCHEDULED` / `OPEN` / `CLOSED` / `SOLD_OUT` / `CANCELLED` / `COMPLETED`
- `ticketCreationStatus` ∈ `EVENT_CREATED` / `SECTION_CREATED` / `SEAT_CREATED` / `COMPLETED`

각 값의 의미와 전이 규칙은 [도메인 enum 정의](../domain-layer/enums.md)를 참고한다.

## 세부 문서

- [셋업 워크플로우](./setup-workflow.md) — 생성 → 구역 → 좌석 → 완료 (셋업 1~4단계)
- [예매 상태 전이](./status-transition.md) — open / close / cancel, 수정(PUT)
- [조회](./queries.md) — 단건 / 목록(필터) 조회
- [에러 레퍼런스](./errors.md) — 에러 포맷 + 도메인 예외 → HTTP 매핑

상위 도메인 문서 인덱스는 [../README.md](../README.md) 를 참고한다.
