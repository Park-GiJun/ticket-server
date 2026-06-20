package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto

import com.gijun.ticketserver.application.ticketevent.dto.command.CreateSectionsCommand
import com.gijun.ticketserver.application.ticketevent.dto.command.CreateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.dto.command.UpdateTicketEventCommand
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
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

/** 구역 일괄 생성 요청(셋업 2단계). */
data class CreateSectionsRequest(
    @field:NotEmpty
    @field:Valid
    val sections: List<SectionItem>,
) {
    data class SectionItem(
        @field:NotBlank
        @field:Size(max = 100)
        val sectionName: String,

        @field:NotBlank
        @field:Size(max = 30)
        val grade: String,

        @field:PositiveOrZero
        val price: Long,

        @field:Min(1)
        val capacity: Int,

        /** 좌석 배치도 행당 좌석 수. 미지정 시 기본 20. */
        @field:Min(1)
        val seatsPerRow: Int = 20,
    )

    // ticketEventId 는 경로 변수에서 받는다.
    fun toCommand(ticketEventId: Long): CreateSectionsCommand = CreateSectionsCommand(
        ticketEventId = ticketEventId,
        sections = sections.map {
            CreateSectionsCommand.SectionSpec(
                sectionName = it.sectionName,
                grade = it.grade,
                price = it.price,
                capacity = it.capacity,
                seatsPerRow = it.seatsPerRow,
            )
        },
    )
}
