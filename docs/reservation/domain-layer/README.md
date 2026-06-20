# 도메인 레이어

> 상위: [예매 도메인 인덱스](../README.md)

티켓 예매의 순수 도메인 모델. 다른 도메인과 동일하게 프레임워크에 의존하지 않는
**순수 Kotlin** 코드로, 예매의 상태 머신·점유 만료·불변식을 담는다.

- 모델은 **불변 data class** 다. 상태 변경은 `copy` 기반 메서드로 새 인스턴스를 반환한다.
- 생성 불변식은 `init` 블록에서 강제하며, 위반 시 `IllegalArgumentException` (→ 400) 이 발생한다.
- 상태 전이는 전이 메서드(`markHeld()`/`confirm()`/`expire()`/`cancel()`/`reject()`)에서 허용 출발 상태를
  검증하고, 위반 시 [`ReservationException`](./exceptions.md) 을 던진다.
- 예매는 **좌석 1개**를 점유하지만, **좌석 상태는 ticket-event-service 가 소유**한다(단일 진실 원천).

## saga 가 상태 머신을 규정한다

좌석 점유·결제·취소가 **서비스 경계를 넘는 saga**(Kafka 이벤트 오케스트레이션 + OpenFeign)로 처리되므로,
예매 생성과 좌석 점유 확정 사이에 **비동기 in-flight 구간**이 존재한다. 이 때문에 도메인 상태 머신은
점유 확정 전 `PENDING` 과 점유 실패 종료 `REJECTED` 를 포함한다.

> 도메인 레이어는 **상태 머신과 불변식만** 정의한다. 실제 흐름 제어(Feign 클라이언트, Kafka 토픽,
> Transactional Outbox, 멱등 소비, 보상 트랜잭션)는 **애플리케이션/인프라 레이어** 책임이며,
> 해당 레이어 문서가 추가되면 여기서 링크한다.

## 세부 문서

| 문서 | 내용 |
|------|------|
| [reservation-model.md](./reservation-model.md) | `ReservationModel` — 필드/불변식/상태 전이표, 좌석 상태 연동, 오케스트레이션 개요 |
| [enums.md](./enums.md) | `ReservationStatus`(6종) — 값 정의와 좌석 상태 매핑 |
| [exceptions.md](./exceptions.md) | `ReservationException` sealed 클래스(4종) |
