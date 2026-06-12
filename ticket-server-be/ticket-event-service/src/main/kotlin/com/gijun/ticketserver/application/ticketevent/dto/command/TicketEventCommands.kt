package com.gijun.ticketserver.application.ticketevent.dto.command

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

/** 구역 일괄 생성(이벤트 생성 단계: EVENT_CREATED → SECTION_CREATED). */
data class CreateSectionsCommand(
    val ticketEventId: Long,
    val sections: List<SectionSpec>,
) {
    data class SectionSpec(
        val sectionName: String,
        val grade: String,
        val price: Long,
        val capacity: Int,
    )
}

/**
 * 좌석 일괄 생성(이벤트 생성 단계: SECTION_CREATED → SEAT_CREATED).
 * 구역별 `capacity` 만큼 좌석을 자동 생성하며, 좌석의 `ticketEventId` 는 소속 구역의 값을 복사한다.
 */
data class CreateSeatsCommand(
    val ticketEventId: Long,
)
