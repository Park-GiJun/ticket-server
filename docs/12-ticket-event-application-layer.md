# 12. 티켓 이벤트 — 애플리케이션 레이어

> ⚠️ 현재는 **아웃바운드 포트(영속성)** 와 **인바운드 유스케이스 인터페이스**까지 정의돼 있다.
> 핸들러 구현(빈 stub 상태)·Command/Result DTO 는 아직 없다.

## 인바운드 포트 (유스케이스)

`application/ticketevent/port/in/`

User 와 동일하게 **1 인터페이스 = 1 함수** 규칙을 따른다([05](./05-application-layer.md) 참고).
입출력은 DTO 도입 전까지 도메인 모델(`TicketEventModel`)·primitive 를 사용한다.

```kotlin
// TicketEventCommandUseCases.kt — 명령(Command)
interface CreateTicketEventUseCase { fun create(ticketEvent: TicketEventModel): TicketEventModel }
interface UpdateTicketEventUseCase { fun update(ticketEvent: TicketEventModel): TicketEventModel }
interface OpenTicketEventUseCase  { fun open(id: Long): TicketEventModel }
interface CloseTicketEventUseCase { fun close(id: Long): TicketEventModel }
interface CancelTicketEventUseCase { fun cancel(id: Long): TicketEventModel }

// TicketEventQueryUseCases.kt — 조회(Query)
interface GetTicketEventUseCase { fun getById(id: Long): TicketEventModel }
interface SearchTicketEventsUseCase {
    fun search(category: TicketEventCategory? = null, status: TicketEventStatus? = null): List<TicketEventModel>
}
```

> 예매 오픈/마감/취소(`open`/`close`/`cancel`)는 도메인 상태 전이라 각각 별도 유스케이스로 둔다.
> **인가(누가 호출할 수 있는가)는 유스케이스가 아니라 웹/보안 계층**에서 처리한다
> (`@AuthenticationPrincipal` + `SecurityConfig`, 관리자 전용은 메서드 보안 등). 유스케이스
> 시그니처에 토큰/사용자를 넣지 않는다.

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

- `handler/` — `TicketEventCommandHandler` / `TicketEventQueryHandler` (현재 빈 stub)
- `dto/` — Command / Query / Result (현재는 도메인 모델을 직접 사용)
- 웹 어댑터(`adapter/in`)와 API 레퍼런스 문서
