# 티켓 이벤트(Ticket Event) 도메인

공연/경기 등 예매 대상인 **티켓 이벤트** 도메인의 설계·구현 문서. User 도메인과 동일하게
헥사고날(Ports & Adapters) + CQRS 구조를 따른다.

문서가 비대해지지 않도록 **레이어별 디렉토리**로 나누고, 각 디렉토리의 `README.md` 가
그 레이어의 인덱스를 제공한다. 세부 사항은 관심사별 문서로 분리돼 있다.

> 상위: [전체 문서 인덱스](../README.md)

## 도메인 한눈에 보기

- **계층 구조**: 이벤트(`TicketEvent`) → 구역(`Section`) → 좌석(`Seat`) 의 1:N:N 트리.
- **두 상태 축**: 예매 진행 상태(`TicketEventStatus`)와 셋업 진행 단계(`TicketCreationStatus`)가 독립적.
- **셋업 4단계**: 이벤트 생성 → 구역 생성 → 좌석 생성 → 완료.
- **좌석 상태**: `AVAILABLE`/`HELD`/`SOLD`/`BLOCKED` — 잔여석/매진은 좌석 상태에서 파생(단일 진실 원천).

## 레이어별 문서

| 레이어 | 인덱스 | 세부 문서 |
|--------|--------|-----------|
| 도메인 | [domain-layer/](./domain-layer/README.md) | [ticket-event-model](./domain-layer/ticket-event-model.md) · [section-model](./domain-layer/section-model.md) · [seat-model](./domain-layer/seat-model.md) · [enums](./domain-layer/enums.md) · [exceptions](./domain-layer/exceptions.md) |
| 애플리케이션 | [application-layer/](./application-layer/README.md) | [use-cases](./application-layer/use-cases.md) · [dto](./application-layer/dto.md) · [handlers](./application-layer/handlers.md) · [ports](./application-layer/ports.md) |
| 인프라스트럭처 | [infrastructure-layer/](./infrastructure-layer/README.md) | [entities](./infrastructure-layer/entities.md) · [repositories](./infrastructure-layer/repositories.md) · [adapters](./infrastructure-layer/adapters.md) |
| API 레퍼런스 | [api-reference/](./api-reference/README.md) | [setup-workflow](./api-reference/setup-workflow.md) · [status-transition](./api-reference/status-transition.md) · [queries](./api-reference/queries.md) · [errors](./api-reference/errors.md) |

## 읽는 순서 (권장)

1. **도메인 레이어** — 모델·불변식·상태 전이부터 이해한다.
2. **애플리케이션 레이어** — 유스케이스(도메인별 핸들러)와 포트.
3. **인프라스트럭처 레이어** — JPA 엔티티/리포지토리/어댑터.
4. **API 레퍼런스** — 실제 엔드포인트와 요청/응답.
