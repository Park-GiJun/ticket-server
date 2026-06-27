package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.application.ticketevent.dto.SeatAvailabilityResult
import com.gijun.ticketserver.application.ticketevent.dto.TicketEventSeatResult

/**
 * 좌석(Seat) 조회(Query) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface GetSeatUseCase {
    /** 이벤트 소속 검증 포함: [id] 좌석이 [ticketEventId] 이벤트 소속이 아니면 없음으로 취급한다. */
    fun getById(ticketEventId: Long, id: Long): TicketEventSeatResult
}

interface ListSeatsByEventUseCase {
    fun listByEvent(ticketEventId: Long): List<TicketEventSeatResult>
}

interface GetSeatAvailabilityUseCase {
    fun getAvailability(ticketEventId: Long): SeatAvailabilityResult
}
