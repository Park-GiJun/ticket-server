# 11. 티켓 이벤트 — 도메인 레이어

티켓 이벤트(공연/경기 등 예매 대상)의 순수 도메인 모델. User 도메인과 동일하게
프레임워크에 의존하지 않는 **순수 Kotlin** 코드로, 예매 일정에 대한 핵심 불변식을 담는다.

> ⚠️ 현재 티켓 이벤트는 **도메인 모델 + 영속성 계층**까지만 구현돼 있다.
> 애플리케이션 유스케이스(핸들러/인바운드 포트/DTO)와 웹 API 는 아직 없다([13](./13-ticket-event-infrastructure-layer.md) 참고).

## TicketEventModel

`domain/model/TicketEventModel.kt`

```kotlin
data class TicketEventModel(
    val id: Long? = null,
    val ticketEventName: String,
    val ticketOpenAt: Instant,        // 예매 오픈 시각
    val ticketClosedAt: Instant,      // 예매 마감 시각
    val ticketEventAt: Instant,       // 실제 이벤트(공연/경기) 시각
    val ticketEventStatus: TicketEventStatus = TicketEventStatus.SCHEDULED,
    val ticketEventCategory: TicketEventCategory,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(ticketEventName.isNotBlank()) { "ticketEventName must not be blank" }
        require(ticketClosedAt.isAfter(ticketOpenAt)) { "ticketClosedAt must be after ticketOpenAt" }
        require(!ticketEventAt.isBefore(ticketClosedAt)) { "ticketEventAt must not be before ticketClosedAt" }
    }

    fun isBookable(at: Instant): Boolean =
        ticketEventStatus == TicketEventStatus.OPEN &&
            !at.isBefore(ticketOpenAt) &&
            at.isBefore(ticketClosedAt)

    fun withStatus(newStatus: TicketEventStatus): TicketEventModel =
        copy(ticketEventStatus = newStatus)
}
```

- **불변 data class** — 상태 변경은 `copy` 기반(`withStatus`)으로.
- **생성 불변식(`init`)**: 이름 공백 금지, 마감은 오픈 이후, 이벤트 시각은 마감 이후(같거나 늦음).
- `isBookable(at)` — 주어진 시각이 `OPEN` 상태이며 오픈~마감 구간(마감 미포함) 안인지 판정.
- `id == null` 은 아직 영속화되지 않은 신규 모델을 의미.

## TicketEventStatus / TicketEventCategory

`domain/enums/TicketEventStatus.kt`, `domain/enums/TicketEventCategory.kt`

User 의 `UserRole`/`UserStatus` 와 동일하게 도메인 공유 enum 으로 `domain/enums/` 에 둔다.

```kotlin
enum class TicketEventStatus {
    SCHEDULED,   // 예매 오픈 전(등록만 된 상태)
    OPEN,        // 예매 진행 중
    CLOSED,      // 예매 마감(오픈 기간 종료)
    SOLD_OUT,    // 전 좌석 매진
    CANCELLED,   // 이벤트 취소
    COMPLETED,   // 이벤트 종료(공연/경기 완료)
}

enum class TicketEventCategory {
    CONCERT, MUSICAL, PLAY, SPORTS, EXHIBITION, FESTIVAL, ETC,
}
```

> 상태 전이(예: `SCHEDULED → OPEN → CLOSED`) 규칙과 도메인 서비스(`TicketEventDomainService`)는
> 애플리케이션 유스케이스가 추가될 때 함께 정의할 예정이다.
