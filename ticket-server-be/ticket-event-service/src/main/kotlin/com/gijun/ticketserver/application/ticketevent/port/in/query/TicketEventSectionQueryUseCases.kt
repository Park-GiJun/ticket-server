package com.gijun.ticketserver.application.ticketevent.port.`in`.query

import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSectionResult

/**
 * 구역(Section) 조회(Query) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface GetSectionUseCase {
    /** 이벤트 소속 검증 포함: [id] 구역이 [ticketEventId] 이벤트 소속이 아니면 없음으로 취급한다. */
    fun getById(ticketEventId: Long, id: Long): TicketEventSectionResult
}

interface ListSectionsByEventUseCase {
    fun listByEvent(ticketEventId: Long): List<TicketEventSectionResult>
}
