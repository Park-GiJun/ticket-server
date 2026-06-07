# 12. 티켓 이벤트 — 애플리케이션 레이어

> ⚠️ 현재는 **아웃바운드 포트(영속성)** 만 정의돼 있다.
> 인바운드 포트(UseCase)·핸들러·DTO 는 아직 없으며, 유스케이스가 추가될 때 함께 정의한다.

## 아웃바운드 포트

`application/ticketevent/port/out/TicketEventPersistencePort.kt`

도메인이 영속성에 요구하는 계약. 구현체는 인프라 레이어의
`TicketEventPersistenceAdapter` 이다([13](./13-ticket-event-infrastructure-layer.md)).

```kotlin
interface TicketEventPersistencePort {
    fun save(ticketEvent: TicketEventModel): TicketEventModel
    fun findById(id: Long): TicketEventModel?
    fun existsById(id: Long): Boolean
}
```

- User 의 `UserPersistencePort` 와 동일한 패턴(도메인 모델 입출력, 식별자 기반 조회).
- 이름·카테고리·기간 등 도메인 고유 조회는 필요해질 때 포트와 리포지토리에 메서드를 추가한다.

## 앞으로 (예정)

- `port.in/` — `TicketEventCommandUseCases` / `TicketEventQueryUseCases` (CQRS)
- `handler/` — `TicketEventCommandHandler` / `TicketEventQueryHandler`
- `dto/` — Command / Query / Result
