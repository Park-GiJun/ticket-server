package com.gijun.ticketserver.application.ticketevent.dto.result

import com.gijun.ticketserver.domain.enums.TicketCreationStatus
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel
import com.gijun.ticketserver.domain.model.TicketEventSectionModel
import java.time.Instant

data class TicketEventResult(
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
        fun from(model: TicketEventModel): TicketEventResult = TicketEventResult(
            id = requireNotNull(model.id) { "저장된 티켓 이벤트에는 id 가 존재해야 합니다" },
            ticketEventName = model.ticketEventName,
            ticketOpenAt = model.ticketOpenAt,
            ticketClosedAt = model.ticketClosedAt,
            ticketEventAt = model.ticketEventAt,
            ticketEventStatus = model.ticketEventStatus,
            ticketCreationStatus = model.ticketCreationStatus,
            ticketEventCategory = model.ticketEventCategory,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
        )
    }
}

/** 구역 생성 결과 1건. */
data class TicketEventSectionResult(
    val id: Long,
    val ticketEventId: Long,
    val sectionName: String,
    val grade: String,
    val price: Long,
    val capacity: Int,
) {
    companion object {
        fun from(model: TicketEventSectionModel): TicketEventSectionResult = TicketEventSectionResult(
            id = requireNotNull(model.id) { "저장된 구역에는 id 가 존재해야 합니다" },
            ticketEventId = model.ticketEventId,
            sectionName = model.sectionName,
            grade = model.grade,
            price = model.price,
            capacity = model.capacity,
        )
    }
}

/** 구역 생성(셋업 2단계) 결과: 생성된 구역들과 갱신된 이벤트 상태. */
data class SectionCreationResult(
    val ticketEvent: TicketEventResult,
    val sections: List<TicketEventSectionResult>,
)

/** 좌석 생성(셋업 3단계) 결과: 생성된 좌석 수와 갱신된 이벤트 상태. */
data class SeatCreationResult(
    val ticketEvent: TicketEventResult,
    val createdSeatCount: Int,
)
