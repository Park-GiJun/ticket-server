package com.gijun.ticketserver.domain.model

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import java.time.Instant

/**
 * 티켓 이벤트 도메인 모델. 영속/웹 등 인프라 관심사로부터 독립적이다.
 *
 * @property ticketOpenAt   예매 오픈 시각
 * @property ticketClosedAt 예매 마감 시각
 * @property ticketEventAt  실제 이벤트(공연/경기) 시각
 */
data class TicketEventModel(
    val id: Long? = null,
    val ticketEventName: String,
    val ticketOpenAt: Instant,
    val ticketClosedAt: Instant,
    val ticketEventAt: Instant,
    val ticketEventStatus: TicketEventStatus = TicketEventStatus.SCHEDULED,
    val ticketEventCategory: TicketEventCategory,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(ticketEventName.isNotBlank()) { "ticketEventName must not be blank" }
        require(ticketClosedAt.isAfter(ticketOpenAt)) {
            "ticketClosedAt must be after ticketOpenAt"
        }
        require(!ticketEventAt.isBefore(ticketClosedAt)) {
            "ticketEventAt must not be before ticketClosedAt"
        }
    }

    /** 주어진 시각 기준으로 예매 가능 기간인지 여부. */
    fun isBookable(at: Instant): Boolean =
        ticketEventStatus == TicketEventStatus.OPEN &&
            !at.isBefore(ticketOpenAt) &&
            at.isBefore(ticketClosedAt)

    fun withStatus(newStatus: TicketEventStatus): TicketEventModel =
        copy(ticketEventStatus = newStatus)
}
