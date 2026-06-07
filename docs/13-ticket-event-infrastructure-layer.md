# 13. 티켓 이벤트 — 인프라스트럭처 레이어

`TicketEventPersistencePort`([12](./12-ticket-event-application-layer.md))의 JPA 구현.
User 영속성 어댑터([06](./06-infrastructure-layer.md))와 동일한 구조를 따른다.

## 영속성 (JPA)

`infrastructure/adapter/out/ticketevent/persistence/`

### TicketEventEntity (`entity/`)

```kotlin
@Entity
@Table(name = "ticket_events")
class TicketEventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var ticketEventName: String,
    @Column(nullable = false) var ticketOpenAt: Instant,
    @Column(nullable = false) var ticketClosedAt: Instant,
    @Column(nullable = false) var ticketEventAt: Instant,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var ticketEventStatus: TicketEventStatus,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var ticketEventCategory: TicketEventCategory,
    @CreationTimestamp @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @UpdateTimestamp @Column(nullable = false) var updatedAt: Instant? = null,
) {
    fun toModel(): TicketEventModel { ... }
    companion object { fun fromModel(model: TicketEventModel): TicketEventEntity { ... } }
}
```

- 도메인 `TicketEventModel` ↔ `TicketEventEntity` 변환을 엔티티에 캡슐화(`toModel`/`fromModel`).
- enum 은 `EnumType.STRING` 으로 저장(순서 변경에 안전).
- 생성/수정 시각은 Hibernate `@CreationTimestamp`/`@UpdateTimestamp` 로 자동 관리.

### TicketEventPersistenceRepository (`repository/`)

```kotlin
interface TicketEventPersistenceRepository : JpaRepository<TicketEventEntity, Long>
```

> 아직 식별자 기반 기본 메서드(`findById`/`existsById`/`save`)만 사용한다.
> 도메인 고유 조회가 필요해지면 쿼리 메서드를 추가한다.

### TicketEventPersistenceAdapter (`adapter/`)

`@Component`, `TicketEventPersistencePort` 구현. 엔티티↔도메인 변환을 담당.

```kotlin
override fun save(ticketEvent: TicketEventModel): TicketEventModel {
    val entity = ticketEvent.id
        ?.let { id -> repository.findById(id).orElse(null)?.applyFrom(ticketEvent) }
        ?: TicketEventEntity.fromModel(ticketEvent)
    return repository.save(entity).toModel()
}
```

> User 어댑터와 동일하게 `save` 는 **id 가 있으면 기존 엔티티를 로드해 변경분을 반영(dirty checking)**,
> 없으면 신규 생성한다. `applyFrom` 으로 이름/일정/상태/카테고리를 갱신하며 createdAt 은 보존된다.
