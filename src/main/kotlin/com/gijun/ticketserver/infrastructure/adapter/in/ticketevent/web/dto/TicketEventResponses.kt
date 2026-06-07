package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto

import com.gijun.ticketserver.application.ticketevent.dto.TicketEventResult
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import java.time.Instant

data class TicketEventResponse(
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
        fun from(result: TicketEventResult): TicketEventResponse = TicketEventResponse(
            id = result.id,
            ticketEventName = result.ticketEventName,
            ticketOpenAt = result.ticketOpenAt,
            ticketClosedAt = result.ticketClosedAt,
            ticketEventAt = result.ticketEventAt,
            ticketEventStatus = result.ticketEventStatus,
            ticketEventCategory = result.ticketEventCategory,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt,
        )
    }
}
