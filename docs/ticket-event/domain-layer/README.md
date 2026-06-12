# 티켓 이벤트 — 도메인 레이어

> 상위: [티켓 이벤트 도메인 인덱스](../README.md)

티켓 이벤트(공연/경기 등 예매 대상)의 순수 도메인 모델. User 도메인과 동일하게
프레임워크에 의존하지 않는 **순수 Kotlin** 코드로, 예매 일정/상태와 좌석 구성에 대한 핵심 불변식을 담는다.

## 계층 구조

티켓 이벤트는 **이벤트 → 구역(Section) → 좌석(Seat)** 의 1:N:N 계층으로 구성되며,
세 모델 모두 `domain/model/` 에 둔다.

```
TicketEventModel (1)
   └── TicketEventSectionModel (N)   // 등급/가격 정책의 단위
          └── TicketEventSeatModel (N)   // 예매 흐름의 최소 단위
```

- 모든 모델은 **불변 data class** 다. 상태 변경은 `copy` 기반 메서드로 새 인스턴스를 반환한다.
- 생성 불변식은 각 모델의 `init` 블록에서 강제하며, 위반 시 `IllegalArgumentException` (→ 400) 이 발생한다.
- 상태 전이는 전이 메서드(`open()`/`hold()` 등)에서 허용 출발 상태를 검증하고, 위반 시
  도메인 예외([`TicketEventException`](./exceptions.md)) 를 던진다.
- 잔여석 카운트 등 집계는 **좌석 상태(단일 진실 원천)에서 파생**하며, 상위 모델이 카운트를 보유하지 않는다.

## 세부 문서

| 문서 | 내용 |
|------|------|
| [ticket-event-model.md](./ticket-event-model.md) | `TicketEventModel` — 필드/불변식/`isBookable`/`isCreationCompleted` 와 두 상태 축(예매 상태·셋업 단계) 전이표 |
| [section-model.md](./section-model.md) | `TicketEventSectionModel` — 구역 모델의 필드와 불변식 |
| [seat-model.md](./seat-model.md) | `TicketEventSeatModel` — 좌석 모델, `ticketEventId` 비정규화 근거, 좌석 상태 전이표 |
| [enums.md](./enums.md) | `TicketEventStatus` / `TicketCreationStatus` / `SeatStatus` / `TicketEventCategory` |
| [exceptions.md](./exceptions.md) | `TicketEventException` sealed 클래스(5종) |
