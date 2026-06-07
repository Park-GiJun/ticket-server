# 12. 티켓 이벤트 — 애플리케이션 레이어

User 와 동일한 CQRS 구조(포트 in/out · DTO · 핸들러)를 갖춘다.

## 인바운드 포트 (유스케이스)

`application/ticketevent/port/in/`

User 와 동일하게 **1 인터페이스 = 1 함수** 규칙을 따른다([05](./05-application-layer.md) 참고).
입출력은 Command/Result DTO 를 사용한다(상태 전이·단건 조회는 식별자 `id: Long`).

```kotlin
// TicketEventCommandUseCases.kt — 명령(Command)
interface CreateTicketEventUseCase { fun create(command: CreateTicketEventCommand): TicketEventResult }
interface UpdateTicketEventUseCase { fun update(command: UpdateTicketEventCommand): TicketEventResult }
interface OpenTicketEventUseCase  { fun open(id: Long): TicketEventResult }
interface CloseTicketEventUseCase { fun close(id: Long): TicketEventResult }
interface CancelTicketEventUseCase { fun cancel(id: Long): TicketEventResult }

// TicketEventQueryUseCases.kt — 조회(Query)
interface GetTicketEventUseCase { fun getById(id: Long): TicketEventResult }
interface SearchTicketEventsUseCase { fun search(query: SearchTicketEventsQuery): List<TicketEventResult> }
```

> 예매 오픈/마감/취소(`open`/`close`/`cancel`)는 도메인 상태 전이라 각각 별도 유스케이스로 둔다.
> **인가(누가 호출할 수 있는가)는 유스케이스가 아니라 웹/보안 계층**에서 처리한다
> (`SecurityConfig`, 관리자 전용은 메서드 보안 등). 유스케이스 시그니처에 토큰/사용자를 넣지 않는다.

## DTO

`application/ticketevent/dto/`

| 파일 | 타입 |
|------|------|
| `TicketEventCommands.kt` | `CreateTicketEventCommand`, `UpdateTicketEventCommand` |
| `TicketEventQueries.kt` | `SearchTicketEventsQuery`(`category`/`status` 필터, 둘 다 nullable) |
| `TicketEventResults.kt` | `TicketEventResult`(+`from(TicketEventModel)`) |

## 핸들러

- **`TicketEventCommandHandler`** (`@Service @Transactional`) — 명령 유스케이스 5개 구현.
  `create` 는 신규 모델 저장, `update` 는 기존 엔티티를 로드해 **편집 필드만 갱신**(상태·생성시각 보존),
  `open`/`close`/`cancel` 은 조회 후 도메인 상태 전이([11](./11-ticket-event-domain-layer.md))를 적용해 저장.
- **`TicketEventQueryHandler`** (`@Service @Transactional(readOnly = true)`) — `getById`(없으면
  `TicketEventNotFound`), `search`(카테고리/상태 필터).

## 아웃바운드 포트

`application/ticketevent/port/out/TicketEventPersistencePort.kt`

도메인이 영속성에 요구하는 계약. 구현체는 인프라 레이어의
`TicketEventPersistenceAdapter` 이다([13](./13-ticket-event-infrastructure-layer.md)).

```kotlin
interface TicketEventPersistencePort {
    fun save(ticketEvent: TicketEventModel): TicketEventModel
    fun findById(id: Long): TicketEventModel?
    fun existsById(id: Long): Boolean
    fun search(category: TicketEventCategory?, status: TicketEventStatus?): List<TicketEventModel>
}
```

- User 의 `UserPersistencePort` 와 동일한 패턴(도메인 모델 입출력, 식별자 기반 조회).
- `search(category, status)` 는 카테고리/상태 필터 조회. 리포지토리 JPQL 로 구현 → [13](./13-ticket-event-infrastructure-layer.md).

## 웹 API

REST 어댑터(`adapter/in/ticketevent/web`)와 엔드포인트 명세는 [14. API 레퍼런스](./14-ticket-event-api-reference.md) 참고.
