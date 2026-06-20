# 예매(Reservation) 도메인

`reservation-service` 가 담당하는 **티켓 예매** 도메인의 설계·구현 문서.
다른 서비스와 동일하게 헥사고날(Ports & Adapters) + CQRS 구조를 따른다.

> 상위: [전체 문서 인덱스](../README.md)

## 도메인 한눈에 보기

- **예매 단위 = 좌석 1개**. 한 예매(`ReservationModel`)는 정확히 하나의 좌석을 점유한다.
  여러 좌석을 한 번에 사려면 여러 건의 예매를 생성한다(묶음은 상위 결제/주문 개념에서 처리).
- **별도 최상위 애그리거트**: ticket-event(이벤트·구역·좌석)와 동등한 별도 서비스/도메인이며,
  도메인 간 연결은 **ID 참조**(`userId` / `ticketEventId` / `seatId`)로 한다.
- **좌석 상태가 단일 진실 원천(SoT)**: 잔여석·매진 판정은 좌석 상태에서 파생한다. 좌석 상태는
  ticket-event-service 가 소유하며, 예매는 사용자 관점의 기록이다.
- **서비스 간 saga 로 정합성 보장**: 좌석 점유·결제·취소는 단일 로컬 트랜잭션이 아니라
  **Kafka 이벤트 오케스트레이션 + OpenFeign**(동기 조회)과 **보상 트랜잭션**으로 처리한다.
  그래서 상태 머신에 점유 확정 전 `PENDING`, 점유 실패 `REJECTED` 가 있다.

## 레이어별 문서

| 레이어 | 인덱스 | 세부 문서 |
|--------|--------|-----------|
| 도메인 | [domain-layer/](./domain-layer/README.md) | [reservation-model](./domain-layer/reservation-model.md) · [enums](./domain-layer/enums.md) · [exceptions](./domain-layer/exceptions.md) |

> 애플리케이션/인프라/API 레퍼런스 레이어는 구현 진행에 따라 추가한다(saga 오케스트레이션·Outbox·Feign 클라이언트 포함).
