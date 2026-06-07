package com.gijun.ticketserver.application.ticketevent.dto

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel
import java.time.Instant

data class TicketEventResult(
    val id: Long,
    val ticketEventName: String,
    val ticketOpenAt: Instant,
    val ticketClosedAt: Instant,
    val ticketEventAt: Instant,
    val ticketEventStatus: TicketEventStatus,
    val ticketEventCategory: TicketEventCategory,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(model: TicketEventModel): TicketEventResult = TicketEventResult(
            id = requireNotNull(model.id) { "저장된 티켓 이벤트에는 id 가 존재해야 합니다" },
            ticketEventName = model.ticketEventName,
            ticketOpenAt = model.ticketOpenAt,
            ticketClosedAt = model.ticketClosedAt,
            ticketEventAt = model.ticketEventAt,
            ticketEventStatus = model.ticketEventStatus,
            ticketEventCategory = model.ticketEventCategory,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
        )
    }
}
