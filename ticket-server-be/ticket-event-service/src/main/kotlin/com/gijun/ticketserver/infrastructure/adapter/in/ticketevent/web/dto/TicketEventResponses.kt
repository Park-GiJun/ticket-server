package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto

import com.gijun.ticketserver.application.ticketevent.dto.result.SeatCreationResult
import com.gijun.ticketserver.application.ticketevent.dto.result.SectionCreationResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSectionResult
import com.gijun.ticketserver.domain.enums.TicketCreationStatus
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
    val ticketCreationStatus: TicketCreationStatus,
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
            ticketCreationStatus = result.ticketCreationStatus,
            ticketEventCategory = result.ticketEventCategory,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt,
        )
    }
}

data class TicketEventSectionResponse(
    val id: Long,
    val ticketEventId: Long,
    val sectionName: String,
    val grade: String,
    val price: Long,
    val capacity: Int,
) {
    companion object {
        fun from(result: TicketEventSectionResult): TicketEventSectionResponse = TicketEventSectionResponse(
            id = result.id,
            ticketEventId = result.ticketEventId,
            sectionName = result.sectionName,
            grade = result.grade,
            price = result.price,
            capacity = result.capacity,
        )
    }
}

/** 구역 생성(셋업 2단계) 응답: 갱신된 이벤트 + 생성된 구역들. */
data class SectionCreationResponse(
    val ticketEvent: TicketEventResponse,
    val sections: List<TicketEventSectionResponse>,
) {
    companion object {
        fun from(result: SectionCreationResult): SectionCreationResponse = SectionCreationResponse(
            ticketEvent = TicketEventResponse.from(result.ticketEvent),
            sections = result.sections.map { TicketEventSectionResponse.from(it) },
        )
    }
}

/** 좌석 생성(셋업 3단계) 응답: 갱신된 이벤트 + 생성된 좌석 수. */
data class SeatCreationResponse(
    val ticketEvent: TicketEventResponse,
    val createdSeatCount: Int,
) {
    companion object {
        fun from(result: SeatCreationResult): SeatCreationResponse = SeatCreationResponse(
            ticketEvent = TicketEventResponse.from(result.ticketEvent),
            createdSeatCount = result.createdSeatCount,
        )
    }
}
