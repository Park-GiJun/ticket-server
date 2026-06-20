# 티켓 이벤트 — 인프라스트럭처 레이어

> 상위: [티켓 이벤트 도메인 인덱스](../README.md)

애플리케이션 레이어 아웃바운드 포트([`../application-layer/ports.md`](../application-layer/ports.md))들의 JPA 구현.
User 영속성 어댑터([user/infrastructure-layer](../../user/infrastructure-layer.md))와 동일한 구조를 따른다.

## 영속성 어댑터 개요

헥사고날 아키텍처의 **out 포트 구현**으로, 도메인을 JPA·Spring Data 에 의존시키지 않기 위한 변환 경계다.
어댑터(`@Component`)가 포트를 구현하고, 그 안에서 엔티티↔도메인 모델을 변환한다. 도메인 모델은
영속성 기술을 전혀 모른다.

`infrastructure/adapter/out/ticketevent/persistence/` 아래 세 종류로 나뉘며,
**이벤트·구역(Section)·좌석(Seat)** 3종을 각각 둔다.

```
persistence/
├── entity/       // JPA @Entity ↔ 도메인 모델 변환(toModel/fromModel)
├── repository/   // Spring Data JpaRepository(파생 쿼리·@Query)
└── adapter/      // @Component 포트 구현(엔티티↔도메인 변환·저장 전략)
```

| 종류 | entity | repository | adapter / 구현 포트 |
|------|--------|------------|---------------------|
| 이벤트 | `TicketEventEntity` | `TicketEventPersistenceRepository` | `TicketEventPersistenceAdapter` → `TicketEventPersistencePort` |
| 구역 | `TicketEventSectionEntity` | `TicketEventSectionPersistenceRepository` | `TicketEventSectionPersistenceAdapter` → `TicketEventSectionPersistencePort` |
| 좌석 | `TicketEventSeatEntity` | `TicketEventSeatPersistenceRepository` | `TicketEventSeatPersistenceAdapter` → `TicketEventSeatPersistencePort` |

## 세부 문서

| 문서 | 내용 |
|------|------|
| [entities.md](./entities.md) | `TicketEventEntity`(`ticketCreationStatus` 컬럼) / `TicketEventSectionEntity`(인덱스) / `TicketEventSeatEntity`(`ticketEventId` 비정규화 + 복합 인덱스) — 테이블명·컬럼·enum 저장 방식 |
| [repositories.md](./repositories.md) | 3개 `JpaRepository` — `search`(null-safe JPQL) / `findByTicketEventId` / `existsByTicketEventIdAndStatus` / `countGroupedByStatus`(`SeatStatusCount` 프로젝션) |
| [adapters.md](./adapters.md) | 3개 어댑터 — `save` 의 dirty checking·`applyFrom` / `saveAll` / 집계 `Map` 변환 |
