package com.gijun.ticketserver.application.ticketevent.port.`in`.query

import com.gijun.ticketserver.application.ticketevent.dto.query.SearchTicketEventsQuery
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventResult

/**
 * 티켓 이벤트 조회(Query) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface GetTicketEventUseCase {
    fun getById(id: Long): TicketEventResult
}

interface SearchTicketEventsUseCase {
    fun search(query: SearchTicketEventsQuery): List<TicketEventResult>
}
