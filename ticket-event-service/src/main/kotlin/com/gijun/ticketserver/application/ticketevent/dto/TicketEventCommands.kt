package com.gijun.ticketserver.application.ticketevent.dto

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import java.time.Instant

data class CreateTicketEventCommand(
    val ticketEventName: String,
    val ticketOpenAt: Instant,
    val ticketClosedAt: Instant,
    val ticketEventAt: Instant,
    val ticketEventCategory: TicketEventCategory,
)

data class UpdateTicketEventCommand(
    val id: Long,
    val ticketEventName: String,
    val ticketOpenAt: Instant,
    val ticketClosedAt: Instant,
    val ticketEventAt: Instant,
    val ticketEventCategory: TicketEventCategory,
)
