package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto

import com.gijun.ticketserver.application.ticketevent.dto.CreateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.dto.UpdateTicketEventCommand
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateTicketEventRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val ticketEventName: String,

    @field:NotNull
    val ticketOpenAt: Instant,

    @field:NotNull
    val ticketClosedAt: Instant,

    @field:NotNull
    val ticketEventAt: Instant,

    @field:NotNull
    val ticketEventCategory: TicketEventCategory,
) {
    fun toCommand(): CreateTicketEventCommand = CreateTicketEventCommand(
        ticketEventName = ticketEventName,
        ticketOpenAt = ticketOpenAt,
        ticketClosedAt = ticketClosedAt,
        ticketEventAt = ticketEventAt,
        ticketEventCategory = ticketEventCategory,
    )
}

data class UpdateTicketEventRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val ticketEventName: String,

    @field:NotNull
    val ticketOpenAt: Instant,

    @field:NotNull
    val ticketClosedAt: Instant,

    @field:NotNull
    val ticketEventAt: Instant,

    @field:NotNull
    val ticketEventCategory: TicketEventCategory,
) {
    // id 는 경로 변수에서 받는다.
    fun toCommand(id: Long): UpdateTicketEventCommand = UpdateTicketEventCommand(
        id = id,
        ticketEventName = ticketEventName,
        ticketOpenAt = ticketOpenAt,
        ticketClosedAt = ticketClosedAt,
        ticketEventAt = ticketEventAt,
        ticketEventCategory = ticketEventCategory,
    )
}
